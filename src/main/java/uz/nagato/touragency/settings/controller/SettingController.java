package uz.nagato.touragency.settings.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uz.nagato.touragency.common.response.ApiResponse;
import uz.nagato.touragency.settings.service.SettingService;

import java.util.Map;

/**
 * Site settings. Reads are public because the storefront renders contact details and
 * social links from them; writes are staff-only.
 */
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingController {

    private final SettingService settingService;

    @GetMapping("/{group}")
    public ApiResponse<Map<String, Object>> get(@PathVariable String group) {
        return ApiResponse.ok(settingService.find(group));
    }

    @PutMapping("/{group}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ApiResponse<Map<String, Object>> update(@PathVariable String group,
                                                   @RequestBody Map<String, Object> values) {
        return ApiResponse.ok("Settings saved", settingService.save(group, values));
    }
}
