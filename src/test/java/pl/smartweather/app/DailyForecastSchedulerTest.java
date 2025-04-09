package pl.smartweather.app;

import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import pl.smartweather.app.model.entity.Weather;
import pl.smartweather.app.service.EmailService;
import pl.smartweather.app.service.WeatherService;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@SpringBootTest(classes = DailyForecastScheduler.class)
public class DailyForecastSchedulerTest {
    @MockitoBean
    private WeatherService weatherService;
    @MockitoBean
    private EmailService emailService;

    @Autowired
    private DailyForecastScheduler scheduler;

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(scheduler, "location", "Warsaw");
        ReflectionTestUtils.setField(scheduler, "userEmail", "user@example.com");
        ReflectionTestUtils.setField(scheduler, "secondUserEmail", "user2@example.com");
    }

    @Test
    void shouldSendWeatherToUsers() throws Exception {
        Weather testWeather = Weather.builder().build();

        when(weatherService.findWeatherByLocationAndDate(any(), any())).thenReturn(testWeather);

        scheduler.sendInfoMail();

        verify(weatherService).saveWeatherRecord("Warsaw");
        verify(emailService).sendWeatherToUser("user@example.com", testWeather);
        verify(emailService).sendWeatherToUser("user2@example.com", testWeather);
    }

    @Test
    void shouldSendErrorMessageWithNotificationMessagingException() throws Exception {
        Weather testWeather = Weather.builder().build();

        when(weatherService.findWeatherByLocationAndDate(any(), any())).thenReturn(testWeather);
        doThrow(new MessagingException("Email exception"))
                .when(emailService).sendWeatherToUser(anyString(), any(Weather.class));

        scheduler.sendInfoMail();
        verify(emailService).sendErrorNotification(
                eq("user@example.com"),
                eq("Weather Messaging Error"),
                contains("Check email service"));
    }

    @Test
    void shouldSendErrorMessageWithNotificationIOException() throws Exception {
        Weather testWeather = Weather.builder().build();

        when(weatherService.findWeatherByLocationAndDate(any(), any())).thenReturn(testWeather);
        doThrow(new IOException("IO exception ")).when(emailService).sendWeatherToUser(anyString(), any(Weather.class));

        scheduler.sendInfoMail();
        verify(emailService).sendErrorNotification(
                eq("user@example.com"),
                eq("Weather Chart Error"),
                contains("Check chart service"));
    }

    @Test
    void shouldSendErrorMessageWithNotificationUnexpectedException() throws Exception {
        Weather testWeather = Weather.builder().build();

        when(weatherService.findWeatherByLocationAndDate(any(), any())).thenReturn(testWeather);
        doThrow(new NullPointerException("Unexpected null")).when(emailService).sendWeatherToUser(anyString(), any(Weather.class));

        scheduler.sendInfoMail();
        verify(emailService).sendErrorNotification(
                eq("user@example.com"),
                eq("Undefined error occurred"),
                contains("Check the following trace"));
    }
}
