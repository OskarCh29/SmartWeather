package pl.smartweather.app.scheduler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import pl.smartweather.app.entity.AppConfig;
import pl.smartweather.app.entity.User;
import pl.smartweather.app.entity.Weather;
import pl.smartweather.app.service.ConfigService;
import pl.smartweather.app.service.EmailService;
import pl.smartweather.app.service.UserService;
import pl.smartweather.app.service.WeatherService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class DailyForecastSchedulerTest {

    @MockitoBean
    private WeatherService weatherService;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private ConfigService configService;

    @MockitoBean
    private UserService userService;

    @Autowired
    private DailyForecastScheduler dailyScheduler;

    @ParameterizedTest
    @CsvFileSource(resources = "/testCase/configFieldsMissing_cases.csv")
    void dailySchedulerShouldNotTakeActionWhenConfigFieldsMissing(boolean isInitialized, String location, String email) {
        AppConfig config = new AppConfig();
        config.setInitialized(isInitialized);
        config.setLocation(location);
        config.setRootEmail(email);
        when(configService.getAppConfig()).thenReturn(config);
        dailyScheduler.sendInfoMail();
    }

    @Test
    void dailySchedulerShouldBuildAndSendMessage() throws Exception {
        AppConfig config = new AppConfig();
        config.setRootEmail("root@example.com");
        config.setRootPassword("testPassword");
        config.setInitialized(true);
        config.setLocation("London");

        User testUser = new User();
        testUser.setEmailAddress("user@example.com");
        List<User> allUsers = List.of(testUser);

        Weather weatherRecord = Weather.builder().build();

        when(configService.getAppConfig()).thenReturn(config);
        when(configService.getApiKey()).thenReturn("ApiKey");
        doNothing().when(weatherService).saveWeatherRecord(anyString(), anyString());
        when(userService.getAllUsers()).thenReturn(allUsers);
        when(weatherService.findWeatherByLocationAndDate(eq("London"),any(LocalDate.class)))
                .thenReturn(Optional.of(weatherRecord));

        dailyScheduler.sendInfoMail();

        verify(configService).getAppConfig();
        verify(weatherService).saveWeatherRecord("London","ApiKey");
        verify(userService).getAllUsers();
        verify(weatherService).findWeatherByLocationAndDate(eq("London"),any(LocalDate.class));
        verify(emailService).sendWeatherToUser("user@example.com",weatherRecord);
    }

    @Test
    void dailySchedulerShouldNotTakeActionSubscribeListEmpty(){
        AppConfig config = new AppConfig();
        config.setRootEmail("root@example.com");
        config.setRootPassword("testPassword");
        config.setInitialized(true);
        config.setLocation("London");

        List<User> allUsers = new ArrayList<>();

        when(configService.getAppConfig()).thenReturn(config);
        when(configService.getApiKey()).thenReturn("ApiKey");
        doNothing().when(weatherService).saveWeatherRecord(anyString(), anyString());
        when(userService.getAllUsers()).thenReturn(allUsers);

        dailyScheduler.sendInfoMail();

        verify(configService).getAppConfig();
        verify(weatherService).saveWeatherRecord("London","ApiKey");
        verify(userService).getAllUsers();
    }

    @Test
    void dailySchedulerShouldNotSendReportDueToEmailProblems() throws Exception{
        AppConfig config = new AppConfig();
        config.setRootEmail("root@example.com");
        config.setRootPassword("testPassword");
        config.setInitialized(true);
        config.setLocation("London");

        User testUser = new User();
        testUser.setEmailAddress("user@example.com");
        List<User> allUsers = List.of(testUser);

        Weather weatherRecord = Weather.builder().build();

        when(configService.getAppConfig()).thenReturn(config);
        when(configService.getApiKey()).thenReturn("ApiKey");
        doNothing().when(weatherService).saveWeatherRecord(anyString(), anyString());
        when(userService.getAllUsers()).thenReturn(allUsers);
        when(weatherService.findWeatherByLocationAndDate(eq("London"),any(LocalDate.class)))
                .thenReturn(Optional.of(weatherRecord));
        doThrow(new MailAuthenticationException("Auth failed")).when(emailService).sendWeatherToUser(any(),any());

        dailyScheduler.sendInfoMail();

        verify(configService).getAppConfig();
        verify(weatherService).saveWeatherRecord("London","ApiKey");
        verify(userService).getAllUsers();
        verify(weatherService).findWeatherByLocationAndDate(eq("London"),any(LocalDate.class));
        verify(emailService).sendWeatherToUser("user@example.com",weatherRecord);
    }

    @Test
    void dailySchedulerFailureWhileSavingAndFetchingData() throws Exception {
        AppConfig config = new AppConfig();
        config.setRootEmail("root@example.com");
        config.setRootPassword("testPassword");
        config.setInitialized(true);
        config.setLocation("London");

        User testUser = new User();
        testUser.setEmailAddress("user@example.com");
        List<User> allUsers = List.of(testUser);

        when(configService.getAppConfig()).thenReturn(config);
        when(configService.getApiKey()).thenReturn("ApiKey");
        doNothing().when(weatherService).saveWeatherRecord(anyString(), anyString());
        when(userService.getAllUsers()).thenReturn(allUsers);
        when(weatherService.findWeatherByLocationAndDate(eq("London"),any(LocalDate.class)))
                .thenReturn(Optional.empty());

        dailyScheduler.sendInfoMail();

        verify(configService).getAppConfig();
        verify(weatherService).saveWeatherRecord("London","ApiKey");
        verify(userService).getAllUsers();
        verify(weatherService).findWeatherByLocationAndDate(eq("London"),any(LocalDate.class));
    }

    @Test
    void dailySchedulerShouldInformAdminAboutAppFailure(){
        AppConfig config = new AppConfig();
        config.setRootEmail("root@example.com");
        config.setRootPassword("testPassword");
        config.setInitialized(true);
        config.setLocation("London");

        when(configService.getAppConfig()).thenReturn(config);
        when(configService.getApiKey()).thenThrow(new SecurityException("Api key missing cannot get weather report"));

        dailyScheduler.sendInfoMail();

        verify(configService).getAppConfig();
        verify(configService).getApiKey();

    }

}
