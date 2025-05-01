package pl.smartweather.app.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import pl.smartweather.app.entity.Weather;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface WeatherRepository extends MongoRepository<Weather, LocalDate> {
    Optional<Weather> findByLocationAndDate(String location, LocalDate date);

}
