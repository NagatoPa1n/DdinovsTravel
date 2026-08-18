package uz.nagato.touragency.auth.dto;

import uz.nagato.touragency.user.entity.Role;
import uz.nagato.touragency.user.entity.User;

public record UserResponse(
        Long id,
        String fullName,
        String email,
        String phone,
        Role role,
        boolean enabled
) {

    public static UserResponse from(User user) {
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.isEnabled()
        );
    }
}
