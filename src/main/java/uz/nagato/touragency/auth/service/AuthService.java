package uz.nagato.touragency.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.nagato.touragency.auth.dto.AuthResponse;
import uz.nagato.touragency.auth.dto.LoginRequest;
import uz.nagato.touragency.auth.dto.PasswordChangeRequest;
import uz.nagato.touragency.auth.dto.ProfileUpdateRequest;
import uz.nagato.touragency.auth.dto.RegisterRequest;
import uz.nagato.touragency.auth.dto.UserResponse;
import uz.nagato.touragency.auth.entity.RefreshToken;
import uz.nagato.touragency.auth.repository.RefreshTokenRepository;
import uz.nagato.touragency.common.exception.BadRequestException;
import uz.nagato.touragency.common.exception.ConflictException;
import uz.nagato.touragency.user.entity.Role;
import uz.nagato.touragency.user.entity.User;
import uz.nagato.touragency.user.repository.UserRepository;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email is already registered: " + email);
        }

        User user = new User();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setPhone(request.phone());
        user.setRole(Role.USER);
        user.setEnabled(true);
        userRepository.save(user);

        return issueTokens(user);
    }

    public AuthResponse login(LoginRequest request) {
        // Delegates credential checking to Spring Security so lockout and encoder rules apply.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email().trim(), request.password()));

        User user = userRepository.findByEmailIgnoreCase(request.email().trim())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));
        if (!user.isEnabled()) {
            throw new DisabledException("Account is disabled");
        }
        return issueTokens(user);
    }

    /** Rotates the refresh token: the presented one is revoked and a fresh pair is returned. */
    public AuthResponse refresh(String refreshTokenValue) {
        RefreshToken stored = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new BadRequestException("Refresh token is invalid"));
        if (!stored.isUsable()) {
            throw new BadRequestException("Refresh token has expired, please sign in again");
        }
        stored.setRevoked(true);
        refreshTokenRepository.save(stored);
        return issueTokens(stored.getUser());
    }

    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });
    }

    public void logoutEverywhere(User user) {
        refreshTokenRepository.revokeAllForUser(user);
    }

    /** Updates the signed-in user's own name, email and phone. */
    public UserResponse updateProfile(User user, ProfileUpdateRequest request) {
        String email = request.email().trim().toLowerCase();
        if (!email.equalsIgnoreCase(user.getEmail())
                && userRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("Email is already registered: " + email);
        }

        user.setFullName(request.name().trim());
        user.setEmail(email);
        if (request.phone() != null) {
            user.setPhone(request.phone().trim());
        }
        return UserResponse.from(userRepository.save(user));
    }

    /**
     * Changes the signed-in user's password after re-checking the current one.
     * Every refresh token is revoked so other sessions cannot keep the old credentials alive.
     */
    public void changePassword(User user, PasswordChangeRequest request) {
        if (!passwordEncoder.matches(request.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (passwordEncoder.matches(request.newPassword(), user.getPassword())) {
            throw new BadRequestException("New password must differ from the current one");
        }

        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        refreshTokenRepository.revokeAllForUser(user);
    }

    private AuthResponse issueTokens(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setToken(generateRefreshTokenValue());
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(Instant.now().plusMillis(refreshTokenExpiration));
        refreshTokenRepository.save(refreshToken);

        return AuthResponse.of(
                jwtService.generateAccessToken(user),
                refreshToken.getToken(),
                jwtService.getAccessTokenExpiration() / 1000,
                UserResponse.from(user));
    }

    private String generateRefreshTokenValue() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return ENCODER.encodeToString(bytes);
    }
}
