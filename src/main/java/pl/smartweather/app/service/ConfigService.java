package pl.smartweather.app.service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pl.smartweather.app.entity.AppConfig;
import pl.smartweather.app.exception.ApiAuthorizationException;
import pl.smartweather.app.exception.ConfigurationException;
import pl.smartweather.app.exception.ExternalException;
import pl.smartweather.app.exception.NoMatchFoundException;
import pl.smartweather.app.model.response.ApiLocationResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigService {
    private final WebClient webClient;
    private final MongoTemplate mongoTemplate;
    private final CryptoService cryptoService;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${security.salt}")
    private String salt;

    @Getter
    private AppConfig appConfig;

    @PostConstruct
    public void init() {
        this.appConfig = mongoTemplate.findById("app_config", AppConfig.class);
        if (appConfig == null) {
            appConfig = new AppConfig();
            mongoTemplate.save(appConfig);
            log.info("First configuration - Initializing empty config file");
        }
    }

    public void checkConfigStatus() {
        if (!appConfig.isInitialized() && appConfig.getRootPassword() == null) {
            throw new ConfigurationException("Application settings not configured");
        }
        if (appConfig.getConfig() == null) {
            throw new ConfigurationException("First configuration not provided yet");
        }
    }

    public Map<String, String> getAppProperties() {
        Map<String, String> properties = new HashMap<>();

        appConfig.getConfig().forEach((k, v) -> {
            if (k.equals("mail_pass") || k.equals("api_key")) {
                properties.put(k, cryptoService.decrypt(v));
            } else {
                properties.put(k, v);
            }
        });
        return properties;
    }

    public void setupAppConfiguration(Map<String, String> configValues) {
        Map<String, String> updateConfig = new HashMap<>();
        configValues.forEach((k, v) -> {
            if (k.equals("mail_pass") || k.equals("api_key")) {
                updateConfig.put(k, cryptoService.encrypt(v));
            } else {
                updateConfig.put(k, v);
            }
        });
        appConfig.setConfig(updateConfig);
        appConfig.setInitialized(true);
        mongoTemplate.save(appConfig);
    }

    public void setRootPassword(String rootPassword) {
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

    public void validateApiKey(String apiKey) {
        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/current.json")
                        .queryParam("key", apiKey)
                        .queryParam("q", "Warsaw")
                        .queryParam("aqi", false)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse ->
                        Mono.error(new ExternalException("API encountered error - check your request")))
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    if (clientResponse.statusCode() == HttpStatus.FORBIDDEN) {
                        return Mono.error(new ApiAuthorizationException("API key is invalid - check configuration"));
                    }
                    return Mono.error(new NoMatchFoundException("No match found on location provided"));
                }).bodyToMono(String.class)
                .block();
    }

    public String getApiKey() {
        if (appConfig.isInitialized()) {
            return cryptoService.decrypt(appConfig.getConfig().get("api_key"));
        } else {
            throw new ConfigurationException("Api key not provided");
        }
    }

    public String getLocation() {
        if (!appConfig.isInitialized() || appConfig.getLocation() == null) {
            throw new ConfigurationException("Location not provided");
        } else {
            return appConfig.getLocation();
        }
    }

    public void setLocationConfiguration(String location) {
        ApiLocationResponse locationResponse = checkIfLocationExists(location);
        appConfig.setLocation(locationResponse.getLocation().getName());
        mongoTemplate.save(appConfig);
    }

    private ApiLocationResponse checkIfLocationExists(String location) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/current.json")
                        .queryParam("key", getApiKey())
                        .queryParam("q", location)
                        .queryParam("aqi", false)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> {
                    if (clientResponse.statusCode() == HttpStatus.BAD_REQUEST) {
                        return Mono.error(new NoMatchFoundException("Provided location not found"));
                    }
                    return Mono.error(new ConfigurationException("Missing information or authorized"));
                }).bodyToMono(ApiLocationResponse.class)
                .block();
    }
}

