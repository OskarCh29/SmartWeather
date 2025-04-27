package pl.smartweather.app;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.smartweather.app.entity.AppConfig;
import pl.smartweather.app.entity.Weather;
import pl.smartweather.app.service.AppConfigService;
import pl.smartweather.app.service.EmailService;
import pl.smartweather.app.service.WeatherService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyForecastScheduler {
    private final WeatherService weatherService;
    private final EmailService emailService;
    private final AppConfigService appConfigService;

    @Value("${userData.location}")
    private String location;

    @Value("${userData.userEmail}")
    private String userEmail;

    @Value("${userData.secondUserEmail}")
    private String secondUserEmail;



    @Scheduled(cron = "0 0 8 * * *")
    public void sendInfoMail() {
        try {
//            if(!appConfigService.isInitialized()){
//                log.warn("Application not configured. Skipping sending daily forecast");
//                return;
//            }
            String today = DateTimeFormatter.ISO_DATE.format(LocalDate.now());
            weatherService.saveWeatherRecord(location);
            Weather weather = weatherService.findWeatherByLocationAndDate(location, today);
            emailService.sendWeatherToUser(userEmail, weather);
            emailService.sendWeatherToUser(secondUserEmail, weather);

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
