package pl.smartweather.app.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LocationRequest {
    @NotBlank
    @Pattern(regexp = "^[^0-9]*$", message = "Location cannot contain numbers")
    @Schema(example = "Warsaw", description = "Weather reports location, validated not to contain numbers")
    private String location;
}
