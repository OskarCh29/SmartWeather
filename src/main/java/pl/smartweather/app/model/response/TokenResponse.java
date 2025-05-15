package pl.smartweather.app.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse {

    @Schema(example = "abc123xyz", description = "Authorization token for validating user session")
    private String userToken;
}
