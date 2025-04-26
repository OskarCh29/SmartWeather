package pl.smartweather.app.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Getter
@Setter
@Document("app_config")
public class AppConfig {

    @Id
    private String id = "app_config";

    private boolean initialized;

    private String rootPassword;

    private Map<String, String> config;
}
