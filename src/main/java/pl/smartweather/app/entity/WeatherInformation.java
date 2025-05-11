package pl.smartweather.app.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.smartweather.app.model.response.WeatherApiResponse;

import java.time.LocalTime;
import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class WeatherInformation {

    public WeatherInformation(WeatherApiResponse response) {
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        WeatherInformation that = (WeatherInformation) o;
        return Double.compare(temperature, that.temperature) == 0
                && Double.compare(feelsLike, that.feelsLike) == 0
                && Double.compare(windSpeed, that.windSpeed) == 0
                && cloud == that.cloud
                && pressure == that.pressure
                && humidity == that.humidity
                && chanceOfRain == that.chanceOfRain
                && Objects.equals(hour, that.hour);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hour, temperature, feelsLike, windSpeed, cloud, pressure, humidity, chanceOfRain);
    }
}
