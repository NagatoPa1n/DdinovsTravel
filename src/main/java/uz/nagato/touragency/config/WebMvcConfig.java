package uz.nagato.touragency.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uz.nagato.touragency.common.convert.CaseInsensitiveEnumConverterFactory;

/** Registers the converters used to bind query parameters. */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        // Registered after the default enum converter, so this one takes precedence.
        registry.addConverterFactory(new CaseInsensitiveEnumConverterFactory());
    }
}
