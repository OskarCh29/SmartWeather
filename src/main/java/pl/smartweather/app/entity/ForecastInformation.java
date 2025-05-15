package pl.smartweather.app.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ForecastInformation {

    @JsonFormat(pattern = "HH:mm")
    @Schema(example = "05:00", description = "Time of sunrise")
    private LocalTime sunrise;

    @JsonFormat(pattern = "HH:mm")
    @Schema(example = "20:00", description = "Time of sunset")
    private LocalTime sunset;

    @Schema(example = "15.0", description = "Maximum temperature observer")
    private double maxTemperature;

    private List<WeatherInformation> hourlyForecast;
}
