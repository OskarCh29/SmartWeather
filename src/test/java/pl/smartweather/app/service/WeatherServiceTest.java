package pl.smartweather.app.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.smartweather.app.exception.BadParametersRequestException;
import pl.smartweather.app.exception.NoMatchFoundException;
import pl.smartweather.app.model.entity.ForecastInformation;
import pl.smartweather.app.model.entity.Weather;
import pl.smartweather.app.model.entity.WeatherInformation;
import pl.smartweather.app.repository.WeatherRepository;
import pl.smartweather.app.utils.TestUtils;

import java.io.IOException;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class WeatherServiceTest {
    @Container
    static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:7.0")
            .withExposedPorts(27017);
    private static final WireMockServer WIRE_MOCK_SERVER = new WireMockServer(wireMockConfig().dynamicPort());

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        MONGO_DB_CONTAINER.start();
        registry.add("spring.data.mongodb.host", MONGO_DB_CONTAINER::getHost);
        registry.add("spring.data.mongodb.port", MONGO_DB_CONTAINER::getFirstMappedPort);
        registry.add("weather.url", WIRE_MOCK_SERVER::baseUrl);
    }

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private WeatherRepository weatherRepository;

    @Autowired
    private WeatherService weatherService;

    @BeforeAll
    public static void setUpWireMockServer() {
        WIRE_MOCK_SERVER.start();
        configureFor("localhost", WIRE_MOCK_SERVER.port());
    }

    @BeforeEach
    public void testSetup() {
        WIRE_MOCK_SERVER.resetAll();
        weatherRepository.deleteAll();
    }

    @Test
    void saveWeatherRecordWhenApiReturnsValidResponse() throws IOException {
        String location = "London";
        var response = TestUtils.getJsonFromFile("/responses/WeatherResponse_200.json");
        WIRE_MOCK_SERVER.stubFor(get(urlPathEqualTo("/forecast.json"))
                .withQueryParam("key", equalTo("testApiKey"))
                .withQueryParam("q", equalTo(location))
                .withQueryParam("days", equalTo("1"))
                .willReturn(aResponse().withHeader("Content-Type", "application/json").withBody(response)));

        weatherService.saveWeatherRecord(location);

        List<Weather> savedRecord = weatherRepository.findAll();
        assertEquals(1, savedRecord.size());
        assertEquals(location, savedRecord.getFirst().getLocation());
    }

    @Test
    void saveWeatherRecordWhenApiReturnValidResponseRecordExists() throws IOException {
        String location = "London";
        String date = "2025-04-08";
        var response = TestUtils.getJsonFromFile("/responses/WeatherResponse_200.json");
        WIRE_MOCK_SERVER.stubFor(get(urlPathEqualTo("/forecast.json"))
                .withQueryParam("key", equalTo("testApiKey"))
                .withQueryParam("q", equalTo(location))
                .withQueryParam("days", equalTo("1"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(response)));

        createTestWeather();

        LogCaptor logCaptor = LogCaptor.forClass(WeatherService.class);
        weatherService.saveWeatherRecord(location);

        List<String> infoLogs = logCaptor.getInfoLogs();
        assertTrue(infoLogs.stream().anyMatch(log ->
                log.contains("Record already exists for London on 2025-04-08")));

    }

    @Test
    void saveWeatherRecordThrowsBadParametersRequestExceptionStatus() {
        stubFor(get(urlPathEqualTo("/forecast.json"))
                .willReturn(serverError()));
        assertThrows(BadParametersRequestException.class, () -> weatherService.saveWeatherRecord("TestLocation"));
    }

    @Test
    void saveWeatherRecordThrowNoMatchesFoundExceptionStatus404() {
        stubFor(get(urlPathEqualTo("/forecast.json"))
                .willReturn(notFound()));
        assertThrows(NoMatchFoundException.class, () -> weatherService.saveWeatherRecord("TestLocation"));
    }


    @Test
    void findRecordByLocationAndDateRecordExists() {
        Weather weather = createTestWeather();

        Weather foundRecord = weatherService.findWeatherByLocationAndDate(weather.getLocation(), weather.getDate());

        assertNotNull(foundRecord, "Record should be found and not be null");
        assertEquals(weather.getLocation(), foundRecord.getLocation(), "Location should be the same");
        assertEquals(weather.getDate(), foundRecord.getDate(), "Records have the same date");
        assertEquals(weather.getForecastInformation().size(), foundRecord.getForecastInformation().size(),
                "Should have the same weather forecast");
    }

    @Test
    void findRecordByLocationAndDateShouldReturnNull() {
        String location = "TestLocation-NotExists";
        String date = "1999-99-99";
        assertNull(weatherService.findWeatherByLocationAndDate(location, date), "Should not find any record");
    }


    private Weather createTestWeather() {
        WeatherInformation information = new WeatherInformation(
                "00:00", 10, 10, 10, 10, 1000, 10, 0);
        ForecastInformation forecastInformation = new ForecastInformation(
                "05:00", "19:00", 10, List.of(information));
        Weather testWeather = Weather.builder()
                .location("London")
                .date("2025-04-08")
                .weatherInformation(information)
                .forecastInformation(List.of(forecastInformation)).build();
        weatherRepository.save(testWeather);
        return testWeather;
    }

}
