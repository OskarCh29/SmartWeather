package pl.smartweather.app;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.smartweather.app.entity.Weather;
import pl.smartweather.app.service.ConfigService;
import pl.smartweather.app.service.EmailService;
import pl.smartweather.app.service.WeatherService;

import java.io.IOException;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyForecastScheduler {
    private final WeatherService weatherService;
    private final EmailService emailService;
    private final ConfigService configService;

    @Value("${userData.userEmail}")
    private String userEmail;

    @Value("${userData.secondUserEmail}")
    private String secondUserEmail;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendInfoMail() {
        try {
            if (!configService.getAppConfig().isInitialized() || configService.getLocation() == null) {
                log.warn("Application not configured. Skipping sending daily forecast");
                return;
            }
            LocalDate today = LocalDate.now();
            weatherService.saveWeatherRecord(configService.getLocation());
            Weather weather = weatherService.findWeatherByLocationAndDate(configService.getLocation(), today).get();
            emailService.sendWeatherToUser(userEmail, weather);
            emailService.sendWeatherToUser(secondUserEmail, weather);
            log.info("Weather report sent - " + configService.getLocation() + " , " + today);

        } catch (MessagingException e) {
            log.error("Failed to send weather email - Check email parameters");
            emailService.sendErrorNotification(userEmail, "Weather Messaging Error",
                    "Check email service - Exception encountered while sending weather report");
        } catch (IOException e) {
            log.error("Application encountered exception while generating Weather Chart");
            emailService.sendErrorNotification(userEmail, "Weather Chart Error",
                    "Check chart service - Exception Encountered while generating weather chart");
        } catch (Exception e) {
            log.error("Unexpected error occurred - Check Application status");
            emailService.sendErrorNotification(userEmail, "Undefined error occurred",
                    "Check the following trace: " + e);
        }
    }
}
