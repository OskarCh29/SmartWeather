package pl.smartweather.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.smartweather.app.entity.ForecastInformation;
import pl.smartweather.app.entity.Weather;
import pl.smartweather.app.entity.WeatherInformation;
import pl.smartweather.app.exception.ExternalException;
import pl.smartweather.app.exception.NoMatchFoundException;
import pl.smartweather.app.model.response.ForecastResponse;
import pl.smartweather.app.model.response.WeatherResponse;
import pl.smartweather.app.repository.WeatherRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WebClient webClient;
    private final WeatherRepository weatherRepository;
    private final ConfigService configService;
    private final MongoTemplate mongoTemplate;

    public void saveWeatherRecord(String location) {
        Weather weather = getCurrentWeather(location).block();
        boolean exists = weatherRepository
                .findByLocationAndDate(weather.getLocation(), weather.getDate())
                .isPresent();

        if (!exists) {
            weatherRepository.save(weather);
        } else {
            log.info("Record already exists for {} on {}", weather.getLocation(), weather.getDate());
        }
    }

    public Weather findWeatherByLocationAndDate(String location, LocalDate date) {
        return weatherRepository.findByLocationAndDate(location, date).orElse(null);
    }

    private Mono<Weather> getCurrentWeather(String location) {
        String queryLocation = location.trim();
        return webClient.get().uri(uriBuilder -> uriBuilder
                        .path("/forecast.json")
                        .queryParam("key", configService.getApiKey())
                        .queryParam("q", queryLocation)
                        .queryParam("days", 1)
                        .queryParam("aqi", false)
                        .queryParam("alerts", false)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        Mono.error(new ExternalException("API encountered error - check your request")))
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    if (clientResponse.statusCode() == HttpStatus.FORBIDDEN) {
                        return Mono.error(new SecurityException("Provided invalid API key"));
                    }

                    return Mono.error(new NoMatchFoundException("No match found on location provided"));
                })
                .bodyToMono(WeatherResponse.class)
                .map(this::mapToWeatherObject);
    }

    private Weather mapToWeatherObject(WeatherResponse weatherResponse) {
        return Weather.builder()
                .location(weatherResponse.getLocation().getName())
                .date((weatherResponse.getCurrentWeather().getLastUpdate()))
                .weatherInformation(new WeatherInformation(weatherResponse))
                .forecastInformation(mapToForecastInformation(weatherResponse.getForecast()))
                .build();
    }

    private List<ForecastInformation> mapToForecastInformation(ForecastResponse forecastResponse) {
        List<ForecastInformation> forecastInformation = new ArrayList<>();

        forecastResponse.getDayForecast().forEach(dayForecast -> {
                    ForecastInformation forecast = new ForecastInformation();
                    forecast.setSunrise(dayForecast.getAstro().getSunrise());
                    forecast.setSunset(dayForecast.getAstro().getSunset());
                    forecast.setMaxTemperature(dayForecast.getDay().getMaxTemperature());

                    List<WeatherInformation> hourlyWeather = dayForecast.getHourlyForecast()
                            .stream()
                            .map(hour -> new WeatherInformation(hour.getTime(),
                                    hour.getTemperature(),
                                    hour.getFeelsLike(),
                                    hour.getWindSpeed(),
                                    hour.getCloud(),
                                    hour.getPressure(),
                                    hour.getHumidity(),
                                    hour.getChanceOfRain())).toList();
                    forecast.setHourlyForecast(hourlyWeather);
                    forecastInformation.add(forecast);
                }
        );
        return forecastInformation;
    }

}

