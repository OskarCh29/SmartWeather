package pl.smartweather.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.smartweather.app.client.WeatherClient;
import pl.smartweather.app.entity.Weather;
import pl.smartweather.app.repository.WeatherRepository;

import java.time.LocalDate;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherClient weatherClient;
    private final WeatherRepository weatherRepository;

    public void saveWeatherRecord(String location) {
        Weather weather = weatherClient.getCurrentWeather(location).block();

        Optional<Weather> existingRecord = findExistingRecord(weather);
        existingRecord.ifPresentOrElse(record -> updateWeatherIfChanged(record, weather), () ->
                saveNewWeather(weather));
    }

    public Optional<Weather> findWeatherByLocationAndDate(String location, LocalDate date) {
        return weatherRepository.findByLocationAndDate(location, date);
    }

    private void saveNewWeather(Weather weather) {
        weatherRepository.save(weather);
    }

    private void updateWeatherIfChanged(Weather existing, Weather current) {
        if (!current.getWeatherInformation().equals(existing.getWeatherInformation())) {
            existing.setWeatherInformation(current.getWeatherInformation());
            weatherRepository.save(existing);
            log.info("{} - current weather updated", current.getLocation());
        }
    }

    private Optional<Weather> findExistingRecord(Weather weather) {
        return weatherRepository.findByLocationAndDate(weather.getLocation(), weather.getDate());
    }

}

