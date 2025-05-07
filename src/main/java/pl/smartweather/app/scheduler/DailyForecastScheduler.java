package pl.smartweather.app.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.smartweather.app.entity.AppConfig;
import pl.smartweather.app.entity.User;
import pl.smartweather.app.service.ConfigService;
import pl.smartweather.app.service.EmailService;
import pl.smartweather.app.service.UserService;
import pl.smartweather.app.service.WeatherService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyForecastScheduler {
    private final WeatherService weatherService;
    private final EmailService emailService;
    private final ConfigService configService;
    private final UserService userService;

    @Scheduled(cron = "0 0 8 * * *")
    public void sendInfoMail() {
        AppConfig config = configService.getAppConfig();
        try {
            if (!config.isInitialized()
                    || config.getLocation() == null
                    || config.getRootEmail() == null) {
                log.warn("Application not configured. Skipping sending daily forecast");
                return;
            }
            String location = config.getLocation();
            LocalDate today = LocalDate.now();
            weatherService.saveWeatherRecord(location);

            List<User> userList = userService.getAllUsers();
            if (userList.isEmpty()) {
                log.warn("No weather subscribers. Skipping email");
                return;
            }
            weatherService.findWeatherByLocationAndDate(location, today)
                    .ifPresentOrElse(weather -> {
                        userList.forEach(user -> {
                            try {
                                emailService.sendWeatherToUser(user.getEmailAddress(), weather);
                                log.info("Weather report sent to {} for location {} and date {}",
                                        user.getEmailAddress(), config.getLocation(), today);

                            } catch (Exception e) {
                                log.warn("Could not send weather report to {}: {}",
                                        user.getEmailAddress(), e.getMessage());
                            }
                        });
                    }, (() -> {
                        log.error("Could not find weather data after saving it. Skipping emails.");
                    }));

        } catch (Exception e) {
            log.error("Error occurred while distributing weather reports: {}", e.getMessage());
            emailService.sendErrorNotification(configService.getRootEmail(), "Daily Forecast Scheduler Failure",
                    "Your application encountered error while distributing daily reports. \n"
                            + "Check the following error trace: " + e);
        }
    }
}
