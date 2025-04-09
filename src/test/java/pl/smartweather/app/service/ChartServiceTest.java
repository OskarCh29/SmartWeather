package pl.smartweather.app.service;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import pl.smartweather.app.model.entity.WeatherInformation;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
@SpringBootTest(classes = ChartService.class)
public class ChartServiceTest {

    @Autowired
    ChartService chartService;

    @Test
    void shouldGenerateTemperatureChartImage() throws IOException {
        List<WeatherInformation> forecast = List.of(
                new WeatherInformation("00:00", 10.0, 12.0, 30.0, 80, 1010, 60, 10),
                new WeatherInformation("01:00", 11.0, 13.0, 25.0, 77, 1010, 55, 10)
        );
        byte[] result = chartService.generateTemperatureChart(forecast);

        assertNotNull(result, "Generated chart should not be null");
        assertTrue(result.length > 0, "Chart should contain data");
    }

    @Test
    void shouldGenerateRainChartImage() throws IOException {
        List<WeatherInformation> forecast = List.of(
                new WeatherInformation("00:00", 10.0, 12.0, 30.0, 80, 1010, 60, 10),
                new WeatherInformation("01:00", 11.0, 13.0, 25.0, 77, 1010, 55, 10)
        );
        byte[] result = chartService.generateRainChart(forecast);

        assertNotNull(result, "Generated chart should not be null");
        assertTrue(result.length > 0, "Chart should contain data");
    }
}
