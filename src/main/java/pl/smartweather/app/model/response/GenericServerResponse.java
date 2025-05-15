package pl.smartweather.app.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GenericServerResponse {

    @Schema(example = "Server response message", description = "Response message with description")
    private String message;
}
