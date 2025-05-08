package pl.smartweather.app.service;


import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.test.util.ReflectionTestUtils;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private ConfigService configService;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private ChartService chartService;

    @Mock
    private JavaMailSenderImpl mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(configService, templateEngine, chartService);
    }

    @Test
    void initializeMailConfigurationAppPropertiesNotPresent() {
        AppConfig config = new AppConfig();
        config.setInitialized(false);

        when(configService.getAppConfig()).thenReturn(config);

        emailService.initializeMailConfig();

        JavaMailSenderImpl sender = (JavaMailSenderImpl) ReflectionTestUtils.getField(emailService, "mailSender");

        assertNull(sender, "Configuration remain null - config not provided");
    }

    @Test
    void initializeMailConfigurationPropertiesPresentAndSetUp() {
        AppConfig config = createTestConfig();

        when(configService.getAppConfig()).thenReturn(config);

        emailService.initializeMailConfig();

        JavaMailSenderImpl sender = (JavaMailSenderImpl) ReflectionTestUtils.getField(emailService, "mailSender");

        assertNotNull(sender, "New configuration should be saved");
        assertEquals("smtp.gmail.com", sender.getHost(), "Host should be updated");
        assertEquals(587, sender.getPort(), "Port should be updated");
        assertEquals("testMail@example.com", sender.getUsername(), "Sender mail should updated");
    }

    @Test
    void sendWeatherToUser() throws Exception {
        MimeMessage message = new MimeMessage((Session) null);
        ReflectionTestUtils.setField(emailService, "mailSender", mailSender);

        when(mailSender.createMimeMessage()).thenReturn(message);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html>Test html</html>");

        when(chartService.generateTemperatureChart(any())).thenReturn(new byte[]{1, 2, 3});
        when(chartService.generateRainChart(any())).thenReturn(new byte[]{1, 2, 3});

        Weather weather = createTestWeather();
        emailService.sendWeatherToUser("text@example.com", weather);
        verify(mailSender).send(message);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("emailTemplate"), contextCaptor.capture());

        Context contextUsed = contextCaptor.getValue();
        assertInstanceOf(Weather.class, contextUsed.getVariable("weather"));
    }

    @Test
    void sendErrorNotificationMailSuccess() {
        String email = "user@example.com";
        String subject = "Some error occurred";
        String messageText = "Check you app - crash at...";
        ReflectionTestUtils.setField(emailService, "mailSender", mailSender);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        emailService.sendErrorNotification(email, subject, messageText);

        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertEquals(email, message.getTo()[0]);
        assertEquals(subject, message.getSubject());
        assertEquals(messageText, message.getText());
    }

    @Test
    void sendErrorNotificationMailSendingFailed() {
        String email = "user@example.com";
        String subject = "Some error occurred";
        String messageText = "Check you app - crash at...";
        AppConfig invalidSender = createTestConfig();

        when(configService.getAppConfig()).thenReturn(invalidSender);
        emailService.initializeMailConfig();
        ReflectionTestUtils.setField(emailService, "mailSender", mailSender);

        doThrow(new MailAuthenticationException("Auth failed")).when(mailSender).send(any(SimpleMailMessage.class));

        emailService.sendErrorNotification(email, subject, messageText);

        verify(mailSender).send(any(SimpleMailMessage.class));

    }

    @Test
    void validateEmailConfigurationAuthorized() {
        Map<String, String> config = new HashMap<>();
        config.put("mail_host", "smtp.example.com");
        config.put("mail_port", "587");
        config.put("mail_name", "user@example.com");
        config.put("mail_pass", "testPassword");

        JavaMailSenderImpl spySender = Mockito.spy(new JavaMailSenderImpl());

        doNothing().when(spySender).send(any(SimpleMailMessage.class));

        EmailService spyService = Mockito.spy(new EmailService(configService,templateEngine,chartService));
        doReturn(spySender).when(spyService).buildMailConfiguration(config);

        spyService.validateEmailConfiguration(config);

        verify(spySender).send(any(SimpleMailMessage.class));

        JavaMailSenderImpl sender = (JavaMailSenderImpl) ReflectionTestUtils.getField(spyService,"mailSender");
        assertSame(spySender,sender);

    }

    @Test
    void validateEmailConfigurationUnauthorized() {
        Map<String, String> properties = new HashMap<>();
        properties.put("mail_host", "smtp.gmail.com");
        properties.put("mail_port", "587");
        properties.put("mail_name", "testMail@example.com");
        properties.put("mail_pass", "invalidPassword");

        ReflectionTestUtils.setField(emailService, "mailSender", mailSender);

        assertThrows(SecurityException.class, () -> emailService.validateEmailConfiguration(properties));
    }

    private AppConfig createTestConfig() {
        AppConfig config = new AppConfig();
        config.setInitialized(true);

        Map<String, String> properties = new HashMap<>();
        properties.put("mail_host", "smtp.gmail.com");
        properties.put("mail_port", "587");
        properties.put("mail_name", "testMail@example.com");
        properties.put("mail_pass", "testPassword");
        config.setConfig(properties);
        return config;
    }


    private Weather createTestWeather() {
        WeatherInformation information = new WeatherInformation(
                LocalTime.of(10, 10), 10, 10, 10, 10, 1000, 10, 0);
        ForecastInformation forecastInformation = new ForecastInformation(
                LocalTime.of(7, 10), LocalTime.of(19, 10), 10, List.of(information));
        return Weather.builder()
                .location("testLocation")
                .date(LocalDate.now())
                .weatherInformation(information)
                .forecastInformation(List.of(forecastInformation)).build();
    }
}
