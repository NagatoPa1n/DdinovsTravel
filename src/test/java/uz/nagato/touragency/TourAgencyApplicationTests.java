package uz.nagato.touragency;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class TourAgencyApplicationTests {

    @Test
    void contextLoads() {
        // Fails if any bean, JPA mapping or security rule is misconfigured.
    }
}
