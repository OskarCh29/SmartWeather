package pl.smartweather.app.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.smartweather.app.model.entity.ForecastInformation;
import pl.smartweather.app.model.entity.Weather;
import pl.smartweather.app.model.entity.WeatherInformation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = EmailService.class)
public class EmailServiceTest {
    @MockitoBean
    private JavaMailSender sender;

    @MockitoBean
    private TemplateEngine templateEngine;

    @MockitoBean
    private ChartService chartGenerator;

    @Autowired
    private EmailService emailService;

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
                "00:00", 10, 10, 10, 10, 1000, 10, 0);
        ForecastInformation forecastInformation = new ForecastInformation(
                "05:00", "19:00", 10, List.of(information));
        return Weather.builder()
                .location("testLocation")
                .date("2025-04-08")
                .weatherInformation(information)
                .forecastInformation(List.of(forecastInformation)).build();
    }
}

