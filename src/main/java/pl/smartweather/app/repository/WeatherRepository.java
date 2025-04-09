package pl.smartweather.app.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pl.smartweather.app.model.entity.Weather;

import java.util.Optional;

@Repository
public interface WeatherRepository extends MongoRepository<Weather, String> {
    Optional<Weather> findByLocationAndDate(String location, String date);

}
