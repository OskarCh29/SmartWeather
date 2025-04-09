package pl.smartweather.app.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.smartweather.app.exception.BadParametersRequestException;
import pl.smartweather.app.exception.NoMatchFoundException;
import pl.smartweather.app.model.entity.ForecastInformation;
import pl.smartweather.app.model.entity.Weather;
import pl.smartweather.app.model.entity.WeatherInformation;
import pl.smartweather.app.model.response.ForecastResponse;
import pl.smartweather.app.model.response.WeatherResponse;
import pl.smartweather.app.repository.WeatherRepository;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WebClient webClient;
    private final WeatherRepository weatherRepository;

    @Value("${security.API_KEY}")
    private String apiKey;

    public void saveWeatherRecord(String location) {
        Weather weather = getCurrentWeather(location)
                .map(this::mapToWeatherObject)
                .block();
        boolean exists = weatherRepository
                .findByLocationAndDate(weather.getLocation(), weather.getDate())
                .isPresent();

        if (!exists) {
            weatherRepository.save(weather);
        } else {
            log.info("Record already exists for {} on {}", weather.getLocation(), weather.getDate());
        }
    }

    public Weather findWeatherByLocationAndDate(String location, String date) {
        return weatherRepository.findByLocationAndDate(location, date).orElse(null);
    }

    private Mono<WeatherResponse> getCurrentWeather(String location) {
        String queryLocation = location.trim();
        return webClient.get().uri(uriBuilder -> uriBuilder
                        .path("/forecast.json")
                        .queryParam("key", apiKey)
                        .queryParam("q", queryLocation)
                        .queryParam("days", 1)
                        .queryParam("aqi", false)
                        .queryParam("alerts", false)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        Mono.error(new BadParametersRequestException("API encountered error - check your request")))
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse ->
                        Mono.error(new NoMatchFoundException("No matches found for provided location")))
                .bodyToMono(WeatherResponse.class);
    }

    private Weather mapToWeatherObject(WeatherResponse weatherResponse) {
        return Weather.builder()
                .location(weatherResponse.getLocation().getName())
                .date(weatherResponse.getCurrentWeather().getLastUpdate().split(" ")[0])
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
                            .map(hour -> new WeatherInformation(hour.getTime().split(" ")[1],
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

