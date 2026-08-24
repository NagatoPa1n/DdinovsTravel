package uz.nagato.touragency.contact.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * An enquiry from the public contact form.
 *
 * <p>Only a name and a phone number are required: the agency calls back, so an email
 * address and a written message are conveniences rather than necessities. A blank email
 * passes {@code @Email} — the constraint only rejects a value that is present and malformed.
 */
public record ContactRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 120, message = "Name is too long")
        String name,

        @NotBlank(message = "Phone is required")
        @Size(max = 40, message = "Phone is too long")
        String phone,

        @Email(message = "Enter a valid email address")
        @Size(max = 160, message = "Email is too long")
        String email,

        @Size(max = 4000, message = "Message is too long")
        String message
) {
}
