package pl.smartweather.app.exception;

public class ApiAuthorizationException extends RuntimeException {
    public ApiAuthorizationException(String message) {
        super(message);
    }
}
