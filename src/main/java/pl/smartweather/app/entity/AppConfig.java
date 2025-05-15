package pl.smartweather.app.entity;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(example = "root@example.com", description = "Root app email")
    private String rootEmail;

    @Schema(example = "hashedPassword", description = "Root app password")
    private String rootPassword;

    @Schema(example = "true", description = "Determinate if the app is configured")
    private boolean initialized;

    @Schema(example = "Warsaw", description = "Location chose by user")
    private String location;

    @Schema(example = """
            {
             "mail_host": "smtp.example.com",
             "mail_port": "587",
             "mail_name": "user@example.com",
             "mail_pass": "hashedMailPassword",
             "api_key": "abc123xyz"
             }
            """, description = "Application configuration fields")
    private Map<String, String> config;
}
