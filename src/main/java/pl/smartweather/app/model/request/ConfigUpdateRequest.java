package pl.smartweather.app.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.smartweather.app.security.ProperConfig;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConfigUpdateRequest {

    @NotBlank
    private String token;

    @ProperConfig
    private Map<String, String> newConfig;
}
