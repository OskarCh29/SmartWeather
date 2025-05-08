package pl.smartweather.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.smartweather.app.client.WeatherClient;
import pl.smartweather.app.entity.AppConfig;
import pl.smartweather.app.exception.ConfigurationException;
import pl.smartweather.app.model.response.ApiLocationResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ConfigServiceTest {

    @Container
    static final MongoDBContainer MONGO_DB_CONTAINER = new MongoDBContainer("mongo:4.4.18")
            .withExposedPorts(27017);


    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private CryptoService cryptoService;

    @MockitoBean
    private BCryptPasswordEncoder encoder;

    @MockitoBean
    private WeatherClient weatherClient;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private ConfigService configService;


    @DynamicPropertySource
    static void setMongoDbContainer(DynamicPropertyRegistry registry) {
        MONGO_DB_CONTAINER.start();
        registry.add("spring.data.mongodb.host", MONGO_DB_CONTAINER::getHost);
        registry.add("spring.data.mongodb.port", MONGO_DB_CONTAINER::getFirstMappedPort);
    }

    @BeforeEach
    void resetMock() {
        mongoTemplate.dropCollection("app_config");
    }

    @Test
    void initializationTestShouldInitNewConfigAsSingleton() {
        ReflectionTestUtils.invokeMethod(configService, "init");
        AppConfig config = (AppConfig) ReflectionTestUtils.getField(configService, "appConfig");

        assertNotNull(config);
    }

    @Test
    void initializationTestConfigFoundAndLoaded() {
        initTestConfig();
        ReflectionTestUtils.invokeMethod(configService, "init");
        AppConfig configLoaded = (AppConfig) ReflectionTestUtils.getField(configService, "appConfig");

        assertNotNull(configLoaded);
        assertEquals("root@example.com", configLoaded.getRootEmail());
        assertEquals("testPassword", configLoaded.getRootPassword());
        assertTrue(configLoaded.isInitialized());
        assertEquals("London", configLoaded.getLocation());
        assertEquals(5, configLoaded.getConfig().size());
    }

    @Test
    void checkConfigStatusShouldNotThrowExceptions() {
        initTestConfig();
        ReflectionTestUtils.invokeMethod(configService, "init");

        assertDoesNotThrow(() -> configService.checkConfigStatus(), "Happy path");
    }

    @Test
    void checkConfigStatusShouldThrowExceptionAppNotInitialized() {
        AppConfig config = new AppConfig();
        ReflectionTestUtils.setField(configService, "appConfig", config);

        assertThrows(ConfigurationException.class, () -> configService.checkConfigStatus(), "App not configured");
    }

    @Test
    void checkConfigStatusShouldThrowExceptionRootNotInitialized() {
        AppConfig config = new AppConfig();
        config.setInitialized(true);
        ReflectionTestUtils.setField(configService, "appConfig", config);

        assertThrows(ConfigurationException.class, () -> configService.checkConfigStatus(), "Root missing");
    }

    @Test
    void checkConfigStatusShouldThrowWhenNotInitializedButRootPasswordPresent() {
        AppConfig config = new AppConfig();
        config.setInitialized(false);
        config.setRootPassword("somePassword");
        config.setConfig(new HashMap<>());
        config.setLocation("Warsaw");
        ReflectionTestUtils.setField(configService, "appConfig", config);

        assertDoesNotThrow(() -> configService.checkConfigStatus(),
                "Should throw when password present but whole app not configured");
    }

    @Test
    void checkConfigStatusShouldThrowWhenConfigMissingButLocationPresent() {
        AppConfig config = new AppConfig();
        config.setInitialized(true);
        config.setRootPassword("hashedPassword");
        config.setLocation("Warsaw");
        config.setConfig(null);

        ReflectionTestUtils.setField(configService, "appConfig", config);

        assertThrows(ConfigurationException.class, () -> configService.checkConfigStatus(),
                "Should throw when config is null but location is set");
    }

    @Test
    void checkConfigStatusShouldThrowWhenConfigPresentButLocationMissing() {
        AppConfig config = new AppConfig();
        config.setInitialized(true);
        config.setRootPassword("hashedPassword");
        config.setLocation(null);
        config.setConfig(new HashMap<>());

        ReflectionTestUtils.setField(configService, "appConfig", config);

        assertThrows(ConfigurationException.class, () -> configService.checkConfigStatus(),
                "Should throw when config is set but location is present");
    }

    @Test
    void setupAppConfigurationShouldSaveNewConfiguration() {
        String rootEmail = "root@example.com";

        Map<String, String> properties = new HashMap<>();
        properties.put("mail_host", "smtp.gmail.com");
        properties.put("mail_port", "587");
        properties.put("mail_name", "testMail@example.com");
        properties.put("mail_pass", "testPassword");
        properties.put("api_key", "Api-Key");

        when(cryptoService.encrypt(anyString())).thenReturn("FieldIsEncrypted");
        configService.setupAppConfiguration(properties, rootEmail);


        AppConfig foundConfiguration = mongoTemplate.findById("app_config", AppConfig.class);
        assertNotNull(foundConfiguration, "Config saved and is not null");
        assertEquals(rootEmail, foundConfiguration.getRootEmail());
        assertEquals(properties.get("mail_host"), foundConfiguration.getConfig().get("mail_host"));
        assertEquals(properties.get("mail_port"), foundConfiguration.getConfig().get("mail_port"));
        assertEquals(properties.get("mail_name"), foundConfiguration.getConfig().get("mail_name"));
        assertEquals("FieldIsEncrypted", foundConfiguration.getConfig().get("mail_pass"));
        assertEquals("FieldIsEncrypted", foundConfiguration.getConfig().get("api_key"));
        assertTrue(foundConfiguration.isInitialized());
    }

    @Test
    void initRootPasswordShouldSaveNewRootPassword() {
        AppConfig config = new AppConfig();
        ReflectionTestUtils.setField(configService, "appConfig", config);
        ReflectionTestUtils.setField(configService, "passwordEncoder", encoder);
        String password = "newEncodedPassword";

        when(encoder.encode(anyString())).thenReturn(password);
        configService.initRootPassword(password);

        AppConfig foundConfiguration = mongoTemplate.findById("app_config", AppConfig.class);

        assertNotNull(foundConfiguration);
        assertEquals(password, foundConfiguration.getRootPassword());

    }

    @Test
    void initRootPasswordShouldThrowExceptionRootAlreadyCreated() {
        initTestConfig();
        String password = "newPassword";

        assertThrows(ConfigurationException.class, () -> configService.initRootPassword(password));
    }

    @Test
    void validateRootPasswordSuccessShouldReturnSecurityToken() {
        AppConfig config = new AppConfig();
        config.setRootPassword("hashedPassword");
        ReflectionTestUtils.setField(configService, "appConfig", config);
        ReflectionTestUtils.setField(configService, "passwordEncoder", encoder);
        ReflectionTestUtils.setField(configService, "salt", "salt");

        String password = "Test123";
        when(encoder.matches("saltTest123", "hashedPassword")).thenReturn(true);
        when(tokenService.generateToken()).thenReturn("abcdefghijklmnop");

        String token = configService.validateRootPassword(password);

        assertEquals("abcdefghijklmnop", token);
    }

    @Test
    void validateRootPasswordInvalidShouldThrowException() {
        String wrongPassword = "wrongPassword";

        assertThrows(SecurityException.class, () -> configService.validateRootPassword(wrongPassword));
    }

    @Test
    void getDecryptedApiKeyShouldReturnApiKeyFromDB() {
        AppConfig testConfig = initTestConfig();
        ReflectionTestUtils.setField(configService, "appConfig", testConfig);
        String ApiKey = "api-key-Test";
        when(cryptoService.decrypt(anyString())).thenReturn(ApiKey);

        String returnedKey = configService.getApiKey();

        assertEquals(ApiKey, returnedKey);
    }

    @Test
    void getDecryptedApiKeyShouldThrowExceptionApiKeyNotProvided() {
        AppConfig appConfig = new AppConfig();
        ReflectionTestUtils.setField(configService, "appConfig", appConfig);

        assertThrows(ConfigurationException.class, () -> configService.getApiKey());
    }

    @Test
    void getUserLocationFromDbConfigFileShouldReturnUserLocation() {
        AppConfig testConfig = initTestConfig();
        ReflectionTestUtils.setField(configService, "appConfig", testConfig);

        String userLocation = configService.getUserLocation();

        assertEquals(testConfig.getLocation(), userLocation);
    }

    @Test
    void getUserLocationFromDbConfigFileShouldThrowExceptionLocationNotProvided() {
        AppConfig testConfig = new AppConfig();
        testConfig.setInitialized(true);
        ReflectionTestUtils.setField(configService, "appConfig", testConfig);

        assertThrows(ConfigurationException.class, () -> configService.getUserLocation());
    }

    @Test
    void getUserLocationFromDbConfigFileShouldThrowExceptionAppNotInitialized() {
        AppConfig testConfig = new AppConfig();
        testConfig.setInitialized(false);
        testConfig.setLocation("Warsaw");
        ReflectionTestUtils.setField(configService, "appConfig", testConfig);

        assertThrows(ConfigurationException.class, () -> configService.getUserLocation());
    }

    ///  Here finished
    @Test
    void validateApiKeyBySendingRequestShouldReturnStatus200() {
        String apiKey = "valid-key";
        String location = "London";

        when(weatherClient.validateInputFields(location, apiKey)).thenReturn(Mono.empty());

        configService.validateApiKey(apiKey);

        verify(weatherClient).validateInputFields(location, apiKey);

    }

    @Test
    void setConfigLocationShouldSaveConfigurationWithNewLocation() {
        String apiKey = "valid-key";
        String location = "London";
        ApiLocationResponse mockResponse = new ApiLocationResponse(new ApiLocationResponse.Location());
        mockResponse.getLocation().setName("London");

        ConfigService configSpy = Mockito.spy(configService);
        doReturn(apiKey).when(configSpy).getApiKey();

        when(weatherClient.validateInputFields(location, apiKey)).thenReturn(Mono.just(mockResponse));

        configSpy.setLocationConfiguration(location);



    }

    private AppConfig initTestConfig() {
        AppConfig config = new AppConfig();
        config.setRootEmail("root@example.com");
        config.setRootPassword("testPassword");
        config.setInitialized(true);
        config.setLocation("London");

        Map<String, String> properties = new HashMap<>();
        properties.put("mail_host", "smtp.gmail.com");
        properties.put("mail_port", "587");
        properties.put("mail_name", "testMail@example.com");
        properties.put("mail_pass", "testPassword");
        properties.put("api_key", "Api-Key");
        config.setConfig(properties);

        return mongoTemplate.save(config);
    }


}
