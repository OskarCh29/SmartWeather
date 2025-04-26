package pl.smartweather.app.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import pl.smartweather.app.entity.AppConfig;
import pl.smartweather.app.exception.InvalidConfigurationException;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppConfigService {

    private final MongoTemplate mongoTemplate;

    @Value("${security.salt}")
    private String salt;
    private AppConfig appConfig;

    @PostConstruct
    public void load() {
        this.appConfig = mongoTemplate.findById("app_config", AppConfig.class);
        if (!isInitialized()) {
            throw new InvalidConfigurationException("Application not configured");
        }
    }

    public boolean isInitialized() {
        return appConfig != null && appConfig.isInitialized();
    }

    public void configure(Map<String, String> values, String rootPassword) {
        if (!isInitialized()) {
            String hashedPassword = new BCryptPasswordEncoder().encode(salt + rootPassword);
            Map<String, String> encrypted = new HashMap<>();
            values.forEach((k, v) -> {
                if (k.equals("mail_pass") || k.equals("api_key")) {

                } else {
                    encrypted.put(k, v);
                }
                AppConfig config = new AppConfig();
                config.setRootPassword(hashedPassword);
                config.setInitialized(true);
                config.setConfig(values);

                mongoTemplate.save(config);
                this.appConfig = config;
            });
        } else {
            if (!new BCryptPasswordEncoder().matches(salt + rootPassword, appConfig.getRootPassword())) {
                throw new SecurityException("Invalid password");
            }
            Map<String, String> updateConfig = new HashMap<>();
            values.forEach((k, v) -> {
                if (k.equals("mail_pass") || k.equals("api_key")) {

                } else {
                    updateConfig.put(k, v);
                }
            });
            appConfig.setConfig(updateConfig);
            mongoTemplate.save(appConfig);
        }
    }

}
