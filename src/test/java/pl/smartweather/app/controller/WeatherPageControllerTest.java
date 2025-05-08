package pl.smartweather.app.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import pl.smartweather.app.entity.Weather;
import pl.smartweather.app.entity.WeatherInformation;
import pl.smartweather.app.exception.ExternalException;
import pl.smartweather.app.exception.NoMatchFoundException;
import pl.smartweather.app.service.ConfigService;
import pl.smartweather.app.service.WeatherService;

import java.time.LocalDate;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WeatherPageController.class)
@AutoConfigureMockMvc(addFilters = false)
public class WeatherPageControllerTest {
    @MockitoBean
    private ConfigService configService;

    @MockitoBean
    private WeatherService weatherService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getWeatherShouldReturn200withResponse() throws Exception {
        LocalDate today = LocalDate.now();
        Weather weather = Weather.builder()
                .location("TestLocation")
                .date(today)
                .weatherInformation(new WeatherInformation())
                .build();

        when(configService.getUserLocation()).thenReturn("TestLocation");
        doNothing().when(weatherService).saveWeatherRecord(any(),any());
        when(weatherService.findWeatherByLocationAndDate(weather.getLocation(), today))
                .thenReturn(Optional.of(weather));

        mockMvc.perform(MockMvcRequestBuilders.get("/weather")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.location").value("TestLocation"));
    }

    @Test
    void getWeatherShouldReturn400withResponseExternalApiUnavailable() throws Exception {

        when(configService.getUserLocation()).thenReturn("TestLocation");
        doThrow(new ExternalException("API service - unavailable")).when(weatherService).saveWeatherRecord(any(),any());


        mockMvc.perform(MockMvcRequestBuilders.get("/weather")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("API service - unavailable")));
    }

    @Test
    void getWeatherShouldReturn401withResponseApiKeyInvalid() throws Exception {

        when(configService.getUserLocation()).thenReturn("TestLocation");
        doThrow(new SecurityException("Invalid API key provided")).when(weatherService).saveWeatherRecord(any(),any());

        mockMvc.perform(MockMvcRequestBuilders.get("/weather")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("Invalid API key provided")));
    }
    @Test
    void getWeatherShouldReturn404withResponseNoMatchForLocation() throws Exception {

        when(configService.getUserLocation()).thenReturn("TestLocation");
        doThrow(new NoMatchFoundException("Provided location not found")).when(weatherService).saveWeatherRecord(any(),any());

        mockMvc.perform(MockMvcRequestBuilders.get("/weather")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Provided location not found")));
    }
}
