package pl.smartweather.app.entity;

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
@CompoundIndex(name = "location_date_unique", def = "{'location' : 1, 'date' : -1}")
public class Weather {

    @Id
    private String id;

    private String location;

    private LocalDate date;

    private WeatherInformation weatherInformation;

    private List<ForecastInformation> forecastInformation;

}
