package pl.smartweather.app.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConfigUpdateRequest {

    private String rootPassword;
    private Map<String, String> newConfig;
}
