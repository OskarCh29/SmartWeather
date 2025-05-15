package pl.smartweather.app.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import pl.smartweather.app.security.EmailGuard;

@Getter
public class UserRequest {

    @EmailGuard
    @Schema(example = "user@example.com", description = "User unique email")
    private String emailAddress;
}
