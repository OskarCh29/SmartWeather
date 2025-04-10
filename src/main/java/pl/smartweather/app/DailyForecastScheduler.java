package pl.smartweather.app;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.smartweather.app.model.entity.Weather;
import pl.smartweather.app.service.EmailService;
import pl.smartweather.app.service.WeatherService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyForecastScheduler {
    private final WeatherService weatherService;
    private final EmailService emailService;

    @Value("${userData.location}")
    private String location;

    @Value("${userData.userEmail}")
    private String userEmail;

    @Value("${userData.secondUserEmail}")
    private String secondUserEmail;

    @Scheduled(cron = "* * * * * ?")
    public void sendInfoMail() {
        log.info("Schedule class started: ", LocalDateTime.now());
        try {
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
