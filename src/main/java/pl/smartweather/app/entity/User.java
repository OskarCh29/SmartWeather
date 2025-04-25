package pl.smartweather.app.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import pl.smartweather.app.security.EmailGuard;

@Getter
@Setter
@Builder
@Document(collection = "users")
public class User {
    @Id
    private String id;

    @Indexed(unique = true)
    @EmailGuard("Please enter a valid email")
    private String emailAddress;
}
