package pl.smartweather.app.exception;

public class BadParametersRequestException extends RuntimeException {
    public BadParametersRequestException(String message) {
        super(message);
    }
}
