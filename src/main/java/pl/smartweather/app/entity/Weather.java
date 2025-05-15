package pl.smartweather.app.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.util.List;

@Builder
@Getter
@Setter
@Document(collection = "weather")
@CompoundIndex(name = "location_date_unique", def = "{'location' : 1, 'date' : -1}", unique = true)
public class Weather {

    @Id
    @Schema(example = "mongoID")
    private String id;

    @Schema(example = "Warsaw")
    private String location;

    @Schema(example = "2025-01-01")
    private LocalDate date;

    private WeatherInformation weatherInformation;

    private List<ForecastInformation> forecastInformation;

}
