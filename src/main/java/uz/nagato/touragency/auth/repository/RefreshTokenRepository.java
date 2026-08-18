package uz.nagato.touragency.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uz.nagato.touragency.auth.entity.RefreshToken;
import uz.nagato.touragency.user.entity.User;

import java.time.Instant;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("update RefreshToken rt set rt.revoked = true where rt.user = :user and rt.revoked = false")
    void revokeAllForUser(@Param("user") User user);

    @Modifying
    @Query("delete from RefreshToken rt where rt.expiresAt < :cutoff")
    void deleteExpired(@Param("cutoff") Instant cutoff);
}
