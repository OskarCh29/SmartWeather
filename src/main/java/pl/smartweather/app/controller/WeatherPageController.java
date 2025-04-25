package pl.smartweather.app.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.smartweather.app.model.entity.Weather;
import pl.smartweather.app.service.WeatherService;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequiredArgsConstructor
public class WeatherPageController {

    @Value("${userData.location}")
    private String location;

    private final WeatherService weatherService;

    @GetMapping("/api")
    public ResponseEntity<Weather> welcomePage() {
        String today = DateTimeFormatter.ISO_DATE.format(LocalDateTime.now());
        weatherService.saveWeatherRecord(location);
        Weather weather = weatherService.findWeatherByLocationAndDate(location, today);
        return ResponseEntity.ok(weather);
    }
}
