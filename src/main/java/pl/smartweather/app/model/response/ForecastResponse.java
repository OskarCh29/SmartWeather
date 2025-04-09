package pl.smartweather.app.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class ForecastResponse {

    @JsonProperty("forecastday")
    private List<DayForecast> dayForecast;

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DayForecast {
        private Astro astro;

        private Day day;

        @JsonProperty("hour")
        private List<HourlyForecast> hourlyForecast;

    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Day {
        @JsonProperty("maxtemp_c")
        private double maxTemperature;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Astro {
        private String sunrise;
        private String sunset;
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HourlyForecast {

        private String time;

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

        @JsonProperty("chance_of_rain")
        private int chanceOfRain;
    }
}
