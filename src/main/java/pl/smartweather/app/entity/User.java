package pl.smartweather.app.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "users")
public class User {
    @Id
    @Schema(example = "mongoID", description = "User unique id")
    private String id;

    @Indexed(unique = true)
    @Schema(example = "user@example.com", description = "User unique email")
    private String emailAddress;
}
