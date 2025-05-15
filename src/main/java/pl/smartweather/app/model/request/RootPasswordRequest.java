package pl.smartweather.app.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.smartweather.app.security.PasswordGuard;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RootPasswordRequest {

    @PasswordGuard
    @Schema(example = "RootPassword", description = "Password for root validation")
    private String rootPassword;
}
