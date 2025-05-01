package pl.smartweather.app.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

@Getter
public class LocationRequest {
    @NotBlank
    @Pattern(regexp = "^[^0-9]*$", message = "Location cannot contain numbers")
    private String location;
}
