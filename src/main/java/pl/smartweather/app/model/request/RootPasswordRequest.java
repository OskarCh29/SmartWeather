package pl.smartweather.app.model.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.smartweather.app.security.PasswordGuard;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RootPasswordRequest {

    @PasswordGuard
    private String rootPassword;
}
