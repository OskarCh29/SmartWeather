package pl.smartweather.app.model.request;

import lombok.Getter;
import pl.smartweather.app.security.EmailGuard;

@Getter
public class UserRequest {

    @EmailGuard
    private String emailAddress;
}
