package pl.smartweather.app.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.smartweather.app.security.ProperConfig;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConfigUpdateRequest {

    private String rootPassword;

    @ProperConfig
    private Map<String, String> newConfig;
}
