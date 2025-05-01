package pl.smartweather.app.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.smartweather.app.model.response.WeatherResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WeatherInformation {

    public WeatherInformation(WeatherResponse response) {
        this.hour = LocalTime.now();
        this.temperature = response.getCurrentWeather().getTemperature();
        this.feelsLike = response.getCurrentWeather().getFeelsLike();
        this.windSpeed = response.getCurrentWeather().getWindSpeed();
        this.cloud = response.getCurrentWeather().getCloud();
        this.pressure = response.getCurrentWeather().getPressure();
        this.humidity = response.getCurrentWeather().getHumidity();
    }
    @JsonFormat(pattern = "HH:mm")
    private LocalTime hour;

    private double temperature;

    private double feelsLike;

    private double windSpeed;

    private int cloud;

    private int pressure;

    private int humidity;

    private int chanceOfRain;
}
