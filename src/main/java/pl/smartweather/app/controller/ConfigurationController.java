package pl.smartweather.app.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.smartweather.app.entity.AppConfig;
import pl.smartweather.app.model.request.ConfigUpdateRequest;
import pl.smartweather.app.model.request.LocationRequest;
import pl.smartweather.app.model.request.RootPasswordRequest;
import pl.smartweather.app.model.response.GenericServerResponse;
import pl.smartweather.app.model.response.TokenResponse;
import pl.smartweather.app.service.ConfigService;
import pl.smartweather.app.service.EmailService;
import pl.smartweather.app.service.TokenService;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ConfigurationController {
    private final ConfigService configService;
    private final EmailService emailService;
    private final TokenService tokenService;

    @GetMapping("/config")
    public ResponseEntity<AppConfig> checkConfigStatus() {
        configService.checkConfigStatus();
        return ResponseEntity.ok(configService.getAppConfig());
    }

    @PostMapping("/config/validate")
    public ResponseEntity<TokenResponse> validateRootPassword(
            @RequestBody @Valid RootPasswordRequest rootRequest) {
        String token =configService.validateRootPassword(rootRequest.getRootPassword());
        return ResponseEntity.ok(new TokenResponse(token));
    }

    @PostMapping("/config/root")
    public ResponseEntity<GenericServerResponse> setRootPassword(@RequestBody @Valid RootPasswordRequest rootPass) {
        configService.setRootPassword(rootPass.getRootPassword());
        return ResponseEntity.ok(new GenericServerResponse("Root password updated"));
    }

    @PostMapping("config/location")
    public ResponseEntity<GenericServerResponse> setAppLocation(@RequestBody @Valid LocationRequest locationRequest) {
        configService.setLocationConfiguration(locationRequest.getLocation());
        return ResponseEntity.ok(new GenericServerResponse("Location updated to: " + locationRequest.getLocation()));
    }

    @PostMapping("/config")
    public ResponseEntity<GenericServerResponse> setupConfiguration(
            @RequestBody @Valid ConfigUpdateRequest configRequest) {
        emailService.validateEmailConfiguration(configRequest.getNewConfig());
        configService.validateApiKey(configRequest.getNewConfig().get("api_key"));
        tokenService.validateToken(configRequest.getToken());
        configService.setupAppConfiguration(configRequest.getNewConfig());
        return ResponseEntity.status(HttpStatus.CREATED).body(new GenericServerResponse("Configuration provided"));
    }
}
