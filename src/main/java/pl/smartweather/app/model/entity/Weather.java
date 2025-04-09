package pl.smartweather.app.model.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Builder
@Getter
@Setter
@Document(collection = "weather")
public class Weather {

    @Id
    private String id;

    private String location;

    private String date;

    private WeatherInformation weatherInformation;

    private List<ForecastInformation> forecastInformation;

}
