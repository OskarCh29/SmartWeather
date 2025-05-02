package pl.smartweather.app.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.smartweather.app.entity.Weather;
import pl.smartweather.app.service.ConfigService;
import pl.smartweather.app.service.WeatherService;

import java.time.LocalDate;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class WeatherPageController {

    private final ConfigService configService;
    private final WeatherService weatherService;

    @GetMapping("/api")
    public ResponseEntity<Weather> welcomePage() {
        LocalDate today = LocalDate.now();
        weatherService.saveWeatherRecord(configService.getLocation());
        Optional<Weather> weather = weatherService.findWeatherByLocationAndDate(
                configService.getLocation(), today);
        return ResponseEntity.ok(weather.get());
    }
}
