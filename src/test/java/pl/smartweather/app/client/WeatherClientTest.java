package pl.smartweather.app.client;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.smartweather.app.configuration.WebConfiguration;
import pl.smartweather.app.entity.Weather;
import pl.smartweather.app.exception.ApiAuthorizationException;
import pl.smartweather.app.exception.ExternalException;
import pl.smartweather.app.exception.NoMatchFoundException;
import pl.smartweather.app.mapper.WeatherResponseToWeatherMapper;
import pl.smartweather.app.model.response.ApiLocationResponse;
import pl.smartweather.app.service.ConfigService;
import pl.smartweather.app.util.TestUtil;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalTime;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.testcontainers.shaded.com.google.common.net.HttpHeaders.CONTENT_TYPE;

@SpringBootTest(classes = {WeatherClient.class, WebConfiguration.class, WeatherResponseToWeatherMapper.class})
public class WeatherClientTest {

    private static final WireMockServer WIRE_MOCK_SERVER = new WireMockServer(WireMockConfiguration.options().dynamicPort());

    @Autowired
    private WeatherClient weatherClient;

    @Autowired
    private WeatherResponseToWeatherMapper mapper;

    @MockitoBean
    private ConfigService configService;

    @BeforeAll
    public static void setUpWireMock() {
        WIRE_MOCK_SERVER.start();
        WireMock.configureFor("localhost", WIRE_MOCK_SERVER.port());
    }

    @BeforeEach
    void resetWireMock() {
        WIRE_MOCK_SERVER.resetAll();
    }

    @DynamicPropertySource
    public static void addDynamicUrl(DynamicPropertyRegistry registry) {
        registry.add("weather.url", WIRE_MOCK_SERVER::baseUrl);
    }

    @Test
    void getCurrentWeatherShouldReturnStatus200WithMappedWeatherObject() throws Exception {
        String location = "London";
        String apiKey = "Api-Key";

        when(configService.getApiKey()).thenReturn(apiKey);

        var mockResponse = TestUtil.getJsonResponseFromFile("/response/ForecastResponse_200.json");

        WIRE_MOCK_SERVER.stubFor(get(urlPathEqualTo("/forecast.json"))
                .withQueryParam("key", equalTo("Api-Key"))
                .withQueryParam("q", equalTo(location))
                .withQueryParam("days", equalTo("1"))
                .withQueryParam("aqi", equalTo("false"))
                .withQueryParam("alerts", equalTo("false"))
                .willReturn(ok().withBody(mockResponse)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        Weather weatherResult = weatherClient.getCurrentWeather(location, apiKey).block();

        assertNotNull(weatherResult);
        assertEquals(location, weatherResult.getLocation());
        assertEquals(LocalDate.of(2025, 4, 8), weatherResult.getDate(),
                "Should return response date deserialized");
        assertEquals(LocalTime.of(6, 19), weatherResult.getForecastInformation().getFirst().getSunrise(),
                "Sunrise should be mapped from 06:19 AM to 06:19");
        assertEquals(LocalTime.of(19, 46), weatherResult.getForecastInformation().getFirst().getSunset(),
                "Sunrise should be mapped from 07:46 PM to 19:46");
        assertEquals(24, weatherResult.getForecastInformation().getFirst().getHourlyForecast().size(),
                "Forecast should be mapped as 24-h forecast");
    }

    @Test
    void getCurrentWeatherShouldReturnStatus400whenApiEncounteredProblems() {
        WIRE_MOCK_SERVER.stubFor(get(urlPathEqualTo("/forecast.json"))
                .willReturn(serverError()));

        StepVerifier.create(weatherClient.getCurrentWeather("Warsaw", "Api-Key"))
                .expectErrorMatches(throwable ->
                        throwable instanceof ExternalException
                                && throwable.getMessage().contains("API encountered error - check your request"))
                .verify();
    }

    @Test
    void getCurrentWeatherShouldReturn403whenInvalidApiKeyProvided() {
        WIRE_MOCK_SERVER.stubFor(get(urlPathEqualTo("/forecast.json"))
                .willReturn(forbidden()));

        StepVerifier.create(weatherClient.getCurrentWeather("Warsaw", "Invalid-ApiKey"))
                .expectErrorMatches(throwable ->
                        throwable instanceof SecurityException
                                && throwable.getMessage().contains("Provided invalid API key"))
                .verify();
    }

    @Test
    void getCurrentWeatherShouldReturn404whenNoMatchForLocationFound() {
        WIRE_MOCK_SERVER.stubFor(get(urlPathEqualTo("/forecast.json"))
                .willReturn(notFound()));

        StepVerifier.create(weatherClient.getCurrentWeather("abcd", "Valid-ApiKey"))
                .expectErrorMatches(throwable -> throwable instanceof NoMatchFoundException
                        && throwable.getMessage().contains("No match found on location provided"))
                .verify();
    }

    @Test
    void validateInputFieldsShouldReturn200withResponse() throws Exception {
        String location = "London";
        String apiKey = "validApiKey";
        var response = TestUtil.getJsonResponseFromFile("/response/WeatherResponse_200.json");

        WIRE_MOCK_SERVER.stubFor(get(urlPathEqualTo("/current.json"))
                        .withQueryParam("key",equalTo(apiKey))
                        .withQueryParam("q",equalTo(location))
                        .withQueryParam("aqi",equalTo("false"))
                .willReturn(ok()
                        .withBody(response)
                        .withHeader(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)));

        ApiLocationResponse locationResponse = weatherClient.validateInputFields(location,apiKey).block();

        assertNotNull(locationResponse);
        assertEquals("London",locationResponse.getLocation().getName());
    }

    @Test
    void validateInputFieldsShouldReturn403apiKeyInvalid(){
        String location = "validLocation";
        String apiKey = "InvalidApiKey";

        WIRE_MOCK_SERVER.stubFor(get(urlPathEqualTo("/current.json"))
                .willReturn(forbidden()));

        StepVerifier.create(weatherClient.validateInputFields(location,apiKey))
                .expectErrorMatches(throwable -> throwable instanceof ApiAuthorizationException
                && throwable.getMessage().contains("API key is invalid - check configuration"))
                .verify();
    }

    @Test
    void validateInputFieldsShouldReturn404locationNotFound(){
        String location = "InvalidLocation";
        String apiKey = "validApiKey";
        WIRE_MOCK_SERVER.stubFor(get(urlPathEqualTo("/current.json"))
                .willReturn(badRequest()));

        StepVerifier.create(weatherClient.validateInputFields(location,apiKey))
                .expectErrorMatches(throwable -> throwable instanceof NoMatchFoundException
                        && throwable.getMessage().contains("Provided location not found"))
                .verify();
    }

    @Test
    void validateInputFieldsShouldReturn401ApiAuthorization(){
        String location = "validLocation";
        String apiKey = "validApiKey";
        WIRE_MOCK_SERVER.stubFor(get(urlPathEqualTo("/current.json"))
                .willReturn(unauthorized()));

        StepVerifier.create(weatherClient.validateInputFields(location,apiKey))
                .expectErrorMatches(throwable -> throwable instanceof ExternalException
                        && throwable.getMessage().contains("Unexpected error during weather API validation"))
                .verify();
    }

}
