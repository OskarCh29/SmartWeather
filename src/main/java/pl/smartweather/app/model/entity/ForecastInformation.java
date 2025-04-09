package pl.smartweather.app.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ForecastInformation {
    private String sunrise;
    private String sunset;
    private double maxTemperature;
    private List<WeatherInformation> hourlyForecast;
}
