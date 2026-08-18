package uz.nagato.touragency.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import uz.nagato.touragency.user.entity.Role;
import uz.nagato.touragency.user.entity.User;
import uz.nagato.touragency.user.repository.UserRepository;

/** Creates the first administrator so a fresh database is usable straight away. */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Bean
    public ApplicationRunner seedAdminUser() {
        return args -> {
            if (userRepository.existsByEmailIgnoreCase(adminEmail)) {
                return;
            }
            User admin = new User();
            admin.setFullName("Administrator");
            admin.setEmail(adminEmail.toLowerCase());
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            log.info("Seeded administrator account: {}", adminEmail);
        };
    }
}
