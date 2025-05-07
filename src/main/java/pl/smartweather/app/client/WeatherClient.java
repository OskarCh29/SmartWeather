package pl.smartweather.app.client;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pl.smartweather.app.entity.Weather;
import pl.smartweather.app.exception.ExternalException;
import pl.smartweather.app.exception.NoMatchFoundException;
import pl.smartweather.app.mapper.WeatherResponseToWeatherMapper;
import pl.smartweather.app.model.response.WeatherApiResponse;
import pl.smartweather.app.service.ConfigService;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class WeatherClient {

    private final WebClient webClient;
    private final ConfigService configService;
    private final WeatherResponseToWeatherMapper mapper;

    public Mono<Weather> getCurrentWeather(String location) {
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
                .bodyToMono(WeatherApiResponse.class)
                .map(mapper::map);
    }
}
