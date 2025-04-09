package pl.smartweather.app.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.smartweather.app.model.response.WeatherResponse;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WeatherInformation {

    public WeatherInformation(WeatherResponse response) {
        this.hour = response.getCurrentWeather().getLastUpdate();
        this.temperature = response.getCurrentWeather().getTemperature();
        this.feelsLike = response.getCurrentWeather().getFeelsLike();
        this.windSpeed = response.getCurrentWeather().getWindSpeed();
        this.cloud = response.getCurrentWeather().getCloud();
        this.pressure = response.getCurrentWeather().getPressure();
        this.humidity = response.getCurrentWeather().getHumidity();
    }
    private String hour;

    private double temperature;

    private double feelsLike;

    private double windSpeed;

    private int cloud;

    private int pressure;

    private int humidity;

    private int chanceOfRain;
}
