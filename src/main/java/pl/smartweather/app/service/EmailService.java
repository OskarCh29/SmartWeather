package pl.smartweather.app.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import pl.smartweather.app.model.entity.Weather;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final ChartService chartService;

    public void sendWeatherToUser(String userEmail, Weather weather) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        byte[] temperatureChart = chartService.generateTemperatureChart(
                weather.getForecastInformation().getFirst().getHourlyForecast());
        byte[] rainChart = chartService.generateRainChart(
                weather.getForecastInformation().getFirst().getHourlyForecast()
        );
        helper.addInline("chart_temperature", new ByteArrayResource(temperatureChart), "image/png");
        helper.addInline("chart_rain", new ByteArrayResource(rainChart), "image/png");

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("weather", weather);

        Context context = new Context();
        context.setVariables(templateModel);
        String htmlContent = templateEngine.process("emailTemplate", context);

        helper.setTo(userEmail);
        helper.setFrom("no-replay@smartWeather.com");
        helper.setSubject("Daily Forecast");
        helper.setText(htmlContent, true);

        mailSender.send(message);
    }
    public void sendErrorNotification(String userEmail, String subject, String notification) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-replay@smartWeather.com");
        message.setTo(userEmail);
        message.setSubject(subject);
        message.setText(notification);
        mailSender.send(message);
    }

}
