package pl.smartweather.app.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.smartweather.app.security.EmailGuard;
import pl.smartweather.app.security.ProperConfig;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConfigUpdateRequest {

    @EmailGuard(message = "Admin mail missing or is invalid")
    private String rootEmail;

    @ProperConfig
    private Map<String, String> newConfig;
}
