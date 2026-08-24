package uz.nagato.touragency.auth.dto;

/**
 * {@code token} is the same value as {@code accessToken}; the admin UI stores
 * {@code token}, while {@code accessToken} is kept for existing consumers.
 */
public record AuthResponse(
        String token,
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserResponse user
) {

    public static AuthResponse of(String accessToken, String refreshToken, long expiresIn, UserResponse user) {
        return new AuthResponse(accessToken, accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}
