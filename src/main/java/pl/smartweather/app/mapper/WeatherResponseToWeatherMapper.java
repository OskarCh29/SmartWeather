package pl.smartweather.app.mapper;

import org.springframework.stereotype.Component;
import pl.smartweather.app.entity.ForecastInformation;
import pl.smartweather.app.entity.Weather;
import pl.smartweather.app.entity.WeatherInformation;
import pl.smartweather.app.model.response.ForecastApiResponse;
import pl.smartweather.app.model.response.WeatherApiResponse;

import java.util.ArrayList;
import java.util.List;
@Component
public class WeatherResponseToWeatherMapper {

    public Weather map(WeatherApiResponse weatherApiResponse) {
        return Weather.builder()
                .location(weatherApiResponse.getLocation().getName())
                .date((weatherApiResponse.getCurrentWeather().getLastUpdate()))
                .weatherInformation(new WeatherInformation(weatherApiResponse))
                .forecastInformation(mapToForecastInformation(weatherApiResponse.getForecast()))
                .build();
    }

    private List<ForecastInformation> mapToForecastInformation(ForecastApiResponse forecastApiResponse) {
        List<ForecastInformation> forecastInformation = new ArrayList<>();

        forecastApiResponse.getDayForecast().forEach(dayForecast -> {
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
