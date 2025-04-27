package pl.smartweather.app.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pl.smartweather.app.entity.AppConfig;
import pl.smartweather.app.exception.ConfigurationException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppConfigService {

    private final MongoTemplate mongoTemplate;
    private final CryptoService cryptoService;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Value("${security.salt}")
    private String salt;

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

    public void validateRootPassword(String rootPassword) {
        if (!passwordEncoder.matches(salt + rootPassword, appConfig.getRootPassword())) {
            throw new SecurityException("Invalid password");
        }
    }

}
