package pl.smartweather.app.service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pl.smartweather.app.client.WeatherClient;
import pl.smartweather.app.entity.AppConfig;
import pl.smartweather.app.exception.ConfigurationException;
import pl.smartweather.app.model.response.ApiLocationResponse;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigService {
    private final WeatherClient weatherClient;
    private final MongoTemplate mongoTemplate;
    private final CryptoService cryptoService;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${security.salt}")
    private String salt;

    @Getter
    private AppConfig appConfig;

    @PostConstruct
    private void init() {
        this.appConfig = mongoTemplate.findById("app_config", AppConfig.class);
        if (appConfig == null) {
            appConfig = new AppConfig();
            mongoTemplate.save(appConfig);
            log.info("First configuration - Initializing empty config file");
        }
    }

    public void checkConfigStatus() {
        if (!appConfig.isInitialized() && appConfig.getRootPassword() == null) {
            throw new ConfigurationException("First configuration not provided");
        }
        if (appConfig.getConfig() == null || appConfig.getLocation() == null) {
            throw new ConfigurationException("Admin need to provide proper configuration");
        }
    }

    public void setupAppConfiguration(Map<String, String> configValues, String rootEmail) {
        Map<String, String> updateConfig = new HashMap<>();
        configValues.forEach((k, v) -> {
            if (k.equals("mail_pass") || k.equals("api_key")) {
                updateConfig.put(k, cryptoService.encrypt(v));
            } else {
                updateConfig.put(k, v);
            }
        });
        appConfig.setRootEmail(rootEmail);
        appConfig.setConfig(updateConfig);
        appConfig.setInitialized(true);
        mongoTemplate.save(appConfig);
    }

    public void initRootPassword(String rootPassword) {
        if (appConfig.getRootPassword() == null) {
            String hashedPassword = passwordEncoder.encode(salt + rootPassword);
            appConfig.setRootPassword(hashedPassword);
            mongoTemplate.save(appConfig);
        } else {
            throw new ConfigurationException("Root password was already initialized");
        }
    }

    public String validateRootPassword(String rootPassword) {
        if (!passwordEncoder.matches(salt + rootPassword, appConfig.getRootPassword())) {
            throw new SecurityException("Invalid password");
        } else {
            return tokenService.generateToken();
        }
    }

    public String getApiKey() {
        if (appConfig.isInitialized()) {
            return cryptoService.decrypt(appConfig.getConfig().get("api_key"));
        } else {
            throw new ConfigurationException("Api key not provided");
        }
    }

    public String getUserLocation() {
        if (appConfig.isInitialized() && appConfig.getLocation() != null) {
            return appConfig.getLocation();
        } else {
            throw new ConfigurationException("Location not provided");
        }
    }

    public void validateApiKey(String apiKey) {
        String validLocation = "London";
        weatherClient.validateInputFields(validLocation, apiKey);
    }

    public void setLocationConfiguration(String location) {
        ApiLocationResponse locationResponse = checkIfLocationExists(location);
        appConfig.setLocation(locationResponse.getLocation().getName());
        mongoTemplate.save(appConfig);
    }

    private ApiLocationResponse checkIfLocationExists(String location) {
        String validApiKey = getApiKey();
        return weatherClient.validateInputFields(location, validApiKey).block();
    }
}

