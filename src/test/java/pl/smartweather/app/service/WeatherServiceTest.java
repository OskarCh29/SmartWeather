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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.smartweather.app.exception.ExternalException;
import pl.smartweather.app.exception.NoMatchFoundException;
import pl.smartweather.app.entity.ForecastInformation;
import pl.smartweather.app.entity.Weather;
import pl.smartweather.app.entity.WeatherInformation;
import pl.smartweather.app.repository.WeatherRepository;
import pl.smartweather.app.util.TestUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class WeatherServiceTest {
    @Container
    static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:4.4.18")
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

    @MockitoBean
    private ConfigService configService;

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
        assertThrows(ExternalException.class, () -> weatherService.saveWeatherRecord("TestLocation"));
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
        LocalDate date = LocalDate.of(1999,12,12);
        assertNull(weatherService.findWeatherByLocationAndDate(location, date), "Should not find any record");
    }


    private Weather createTestWeather() {
        WeatherInformation information = new WeatherInformation(
                LocalTime.of(0,0), 10, 10, 10, 10, 1000, 10, 0);
        ForecastInformation forecastInformation = new ForecastInformation(
                LocalTime.of(5,49), LocalTime.of(19,0), 10, List.of(information));
        Weather testWeather = Weather.builder()
                .location("London")
                .date(LocalDate.of(2025, 4,25))
                .weatherInformation(information)
                .forecastInformation(List.of(forecastInformation)).build();
        weatherRepository.save(testWeather);
        return testWeather;
    }

}
