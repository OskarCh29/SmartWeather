package pl.smartweather.app.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(example = "root@example.com", description = "Root app email")
    private String rootEmail;

    @ProperConfig
    @Schema(example = """
            {
             "mail_host": "smtp.example.com",
             "mail_port": "587",
             "mail_name": "user@example.com",
             "mail_pass": "hashedMailPassword",
             "api_key": "abc123xyz"
             }
            """, description = "Application configuration fields")
    private Map<String, String> newConfig;
}
