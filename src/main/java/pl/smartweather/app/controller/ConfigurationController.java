package pl.smartweather.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.smartweather.app.model.request.ConfigUpdateRequest;
import pl.smartweather.app.model.request.RootPasswordRequest;
import pl.smartweather.app.model.response.GenericServerResponse;
import pl.smartweather.app.service.AppConfigService;
import pl.smartweather.app.service.EmailService;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ConfigurationController {
    private final AppConfigService appConfigService;
    private final EmailService emailService;

    @GetMapping("/config")
    public ResponseEntity<Map<String,String>> checkConfigStatus() {
        appConfigService.checkConfigStatus();
        return ResponseEntity.ok(appConfigService.getAppProperties());
    }

    @PostMapping("/config/root")
    public ResponseEntity<GenericServerResponse> setRootPassword(@RequestBody RootPasswordRequest rootPass) {
        appConfigService.setRootPassword(rootPass.getRootPassword());
        return ResponseEntity.ok(new GenericServerResponse("Root password updated"));
    }

    @PostMapping("config/location")
    public ResponseEntity<GenericServerResponse> setAppLocation(@RequestBody @Valid String location) {
        // Provide location update and validation for existing location --> Check API Response

        return ResponseEntity.ok(new GenericServerResponse("Location updated to: " + location));
    }

    @PostMapping("/config")
    public ResponseEntity<GenericServerResponse> setupConfiguration(@RequestBody @Valid ConfigUpdateRequest configRequest) {
        emailService.sendConfigurationNotification(configRequest.getNewConfig().get("mail_name"));
        appConfigService.setupAppConfiguration(configRequest.getNewConfig(), configRequest.getRootPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(new GenericServerResponse("Configuration provided"));
    }
}
