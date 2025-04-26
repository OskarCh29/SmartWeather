package pl.smartweather.app.exception;

public class InvalidConfigurationException extends RuntimeException {
    public InvalidConfigurationException(String message) {
        super(message);
    }
}
