package pl.smartweather.app.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import pl.smartweather.app.util.LocalDateDeserializer;

import java.time.LocalDate;

@Getter
public class WeatherApiResponse {

    private Location location;

    @JsonProperty("current")
    private CurrentWeather currentWeather;

    @JsonProperty("forecast")
    private ForecastApiResponse forecast;

    @Getter
    public static class Location {

        private String name;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CurrentWeather {

        @JsonProperty("last_updated")
        @JsonDeserialize(using = LocalDateDeserializer.class)
        private LocalDate lastUpdate;

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
