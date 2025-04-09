package pl.smartweather.app.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class WeatherResponse {

    private Location location;

    @JsonProperty("current")
    private CurrentWeather currentWeather;

    @JsonProperty("forecast")
    private ForecastResponse forecast;

    @Getter
    public static class Location {

        private String name;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrentWeather {

        @JsonProperty("last_updated")
        private String lastUpdate;

        @JsonProperty("temp_c")
        private double temperature;

        @JsonProperty("wind_kph")
        private double windSpeed;

        @JsonProperty("pressure_mb")
        private int pressure;

        private int humidity;

        private int cloud;

        @JsonProperty("feelslike_c")
        private double feelsLike;

    }
}
