package pl.smartweather.app.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.smartweather.app.security.ProperConfig;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfigUpdateRequest {

    @NotBlank(message = "Authorization token missing")
    private String token;

    @ProperConfig
    private Map<String, String> newConfig;
}
