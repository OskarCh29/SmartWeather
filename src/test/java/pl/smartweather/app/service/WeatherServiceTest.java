package pl.smartweather.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.smartweather.app.client.WeatherClient;
import pl.smartweather.app.entity.Weather;
import pl.smartweather.app.entity.WeatherInformation;
import pl.smartweather.app.repository.WeatherRepository;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WeatherServiceTest {

    @Container
    static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:4.4.18")
            .withExposedPorts(27017);

    @DynamicPropertySource
    static void setMongoDbContainer(DynamicPropertyRegistry registry) {
        MONGO_DB_CONTAINER.start();
        registry.add("spring.data.mongodb.host", MONGO_DB_CONTAINER::getHost);
        registry.add("spring.data.mongodb.port", MONGO_DB_CONTAINER::getFirstMappedPort);
    }

    @Autowired
    private WeatherService weatherService;

    @Autowired
    private WeatherRepository weatherRepository;

    @MockitoBean
    private WeatherClient weatherClient;

    @BeforeEach
    void clearRecords() {
        weatherRepository.deleteAll();
    }

    @Test
    void saveWeatherRecordWhenRecordDoesNotExist() {
        Weather weather = Weather.builder()
                .location("TestLocation")
                .date(LocalDate.now())
                .build();

        when(weatherClient.getCurrentWeather(anyString(),anyString(),anyInt())).thenReturn(Mono.just(weather));

        weatherService.saveWeatherRecord("TestLocation","Valid Api-Key");
        verify(weatherClient).getCurrentWeather(anyString(),anyString(),anyInt());

    }

    @Test
    void saveWeatherRecordWhenExistUpdate() {
        WeatherInformation currentWeather = new WeatherInformation();
        currentWeather.setTemperature(20);
        Weather current = Weather.builder()
                .location("TestLocation")
                .date(LocalDate.now())
                .weatherInformation(currentWeather)
                .build();

        initTestWeather();

        when(weatherClient.getCurrentWeather(anyString(),anyString(),anyInt())).thenReturn(Mono.just(current));
        weatherService.saveWeatherRecord("TestLocation","Valid-ApiKey");

        Optional<Weather> updatedRecord = weatherRepository
                .findByLocationAndDate(current.getLocation(), LocalDate.now());
        assertTrue(updatedRecord.isPresent());
        assertEquals(20, updatedRecord.get().getWeatherInformation().getTemperature());
    }

    @Test
    void saveWeatherRecordDoNothingWhenNoUpdateNeeded() {
        WeatherInformation currentWeather = new WeatherInformation();
        currentWeather.setTemperature(15);
        Weather current = Weather.builder()
                .location("TestLocation")
                .date(LocalDate.now())
                .weatherInformation(currentWeather)
                .build();

        initTestWeather();

        when(weatherClient.getCurrentWeather(anyString(),anyString(),anyInt())).thenReturn(Mono.just(current));
        weatherService.saveWeatherRecord("TestLocation","Valid Api-Key");

        Optional<Weather> updatedRecord = weatherRepository
                .findByLocationAndDate(current.getLocation(), LocalDate.now());
        assertTrue(updatedRecord.isPresent());
    }

    @Test
    void findWeatherByLocationAndDateRecordFound() {
        initTestWeather();
        String location = "TestLocation";
        LocalDate date = LocalDate.now();

        Optional<Weather> foundWeather = weatherService.findWeatherByLocationAndDate(location, date);

        assertTrue(foundWeather.isPresent());
        assertEquals(location, foundWeather.get().getLocation(), "Locations should be equal");
        assertEquals(date, foundWeather.get().getDate());
    }

    private void initTestWeather() {
        WeatherInformation lastUpdate = new WeatherInformation();
        lastUpdate.setTemperature(15);
        Weather lastWeather = Weather.builder()
                .location("TestLocation")
                .date(LocalDate.now())
                .weatherInformation(lastUpdate)
                .build();
        weatherRepository.save(lastWeather);
    }

}
