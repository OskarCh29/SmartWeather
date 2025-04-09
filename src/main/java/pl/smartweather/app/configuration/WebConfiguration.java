package pl.smartweather.app.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebConfiguration {

    @Value("${weather.url}")
    private String clientURL;

    @Value("${mail.username")
    private String emailUsername;

    @Value("${mail.password")
    private String emailPassword;

    @Bean
    public WebClient webClientSetup() {
        return WebClient.builder().baseUrl(clientURL).build();
    }

}
