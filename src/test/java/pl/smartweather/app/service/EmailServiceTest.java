package pl.smartweather.app.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.smartweather.app.entity.AppConfig;
import pl.smartweather.app.entity.ForecastInformation;
import pl.smartweather.app.entity.Weather;
import pl.smartweather.app.entity.WeatherInformation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = EmailService.class)
public class EmailServiceTest {
    @MockitoBean
    private JavaMailSenderImpl sender;

    @MockitoBean
    private TemplateEngine templateEngine;

    @MockitoBean
    private ChartService chartGenerator;

    @Autowired
    private EmailService emailService;

    @MockitoBean
    private ConfigService configService;

    @BeforeEach
    void setup() {
        when(configService.getAppConfig()).thenReturn(createTestConfig());
    }

    @Test
    void shouldBuildAndSendWeatherReportEmail() throws Exception {
        MimeMessage message = new MimeMessage((Session) null);

        when(sender.createMimeMessage()).thenReturn(message);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test html </html>");

        when(chartGenerator.generateTemperatureChart(any())).thenReturn(new byte[]{1, 2, 3});
        when(chartGenerator.generateRainChart(any())).thenReturn(new byte[]{1, 2, 3});


        Weather testWeather = createTestWeather();
        emailService.sendWeatherToUser("text@example.com", testWeather);
        verify(sender).send(message);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("emailTemplate"), contextCaptor.capture());

        Context contextUsed = contextCaptor.getValue();
        assertInstanceOf(Weather.class, contextUsed.getVariable("weather"));
    }

    @Test
    void shouldBuildAndSendNotificationEmail() {
        String email = "user@example.com";
        String subject = "Some error encountered";
        String messageText = "Something went wrong";

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendErrorNotification(email, subject, messageText);

        verify(sender, times(1)).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();

        assertEquals("no-replay@smartWeather.com", sentMessage.getFrom());
        assertEquals(email, sentMessage.getTo()[0]);
        assertEquals(subject, sentMessage.getSubject());
        assertEquals(messageText, sentMessage.getText());
    }

    private Weather createTestWeather() {
        WeatherInformation information = new WeatherInformation(
                LocalTime.of(0,0), 10, 10, 10, 10, 1000, 10, 0);
        ForecastInformation forecastInformation = new ForecastInformation(
                LocalTime.of(5,49), LocalTime.of(19,0), 10, List.of(information));
        return Weather.builder()
                .location("testLocation")
                .date(LocalDate.of(2025, 4,25))
                .weatherInformation(information)
                .forecastInformation(List.of(forecastInformation)).build();
    }
    private AppConfig createTestConfig(){
        AppConfig config = new AppConfig();
        config.setLocation("London");
        config.setInitialized(true);
        config.setConfig(new HashMap<String,String>());
        return config;
    }
}

