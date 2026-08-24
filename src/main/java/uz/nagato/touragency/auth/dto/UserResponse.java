package uz.nagato.touragency.auth.dto;

import uz.nagato.touragency.user.entity.Role;
import uz.nagato.touragency.user.entity.User;

/**
 * {@code name} and {@code fullName} carry the same value: the admin UI reads {@code name},
 * while {@code fullName} is kept so existing API consumers do not break.
 */
public record UserResponse(
        Long id,
        String name,
        String fullName,
        String email,
        String phone,
        Role role,
        boolean enabled
) {

    public static UserResponse from(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.isEnabled()
        );
    }
}
