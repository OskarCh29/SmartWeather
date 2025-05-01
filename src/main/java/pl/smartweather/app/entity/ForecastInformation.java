package pl.smartweather.app.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private LocalTime sunrise;
    @JsonFormat(pattern = "HH:mm")
    private LocalTime sunset;
    private double maxTemperature;
    private List<WeatherInformation> hourlyForecast;
}
