package pl.smartweather.app.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.smartweather.app.client.WeatherClient;
import pl.smartweather.app.entity.Weather;
import pl.smartweather.app.service.ConfigService;
import pl.smartweather.app.service.WeatherService;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class WeatherPageController {
    private static final int FORECAST_DAYS = 3;
    private final ConfigService configService;
    private final WeatherService weatherService;
    private final WeatherClient weatherClient;

    @GetMapping("/weather")
    public ResponseEntity<Weather> welcomePage() {
        LocalDate today = LocalDate.now();
        String userLocation = configService.getUserLocation();
        weatherService.saveWeatherRecord(userLocation, configService.getApiKey());
        Optional<Weather> weather = weatherService.findWeatherByLocationAndDate(
                configService.getUserLocation(), today);
        return ResponseEntity.ok(weather.get());
    }

    @GetMapping("/weather/forecast")
    public ResponseEntity<Weather> getForecast() {
        String userLocation = configService.getUserLocation();
        String apiKey = configService.getApiKey();
        Weather forecast = weatherClient.getCurrentWeather(userLocation, apiKey, FORECAST_DAYS).block();
        return ResponseEntity.ok(forecast);
    }
}
