package uz.nagato.touragency.contact.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.nagato.touragency.common.response.ApiResponse;
import uz.nagato.touragency.contact.dto.ContactRequest;
import uz.nagato.touragency.contact.service.ContactService;

@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    /** Public: the enquiry form on the website posts here. */
    @PostMapping
    public ApiResponse<Void> submit(@Valid @RequestBody ContactRequest request) {
        contactService.submit(request);
        return ApiResponse.message("Message sent");
    }
}
