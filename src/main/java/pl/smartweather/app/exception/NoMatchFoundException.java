package pl.smartweather.app.exception;

public class NoMatchFoundException extends RuntimeException {
  public NoMatchFoundException(String message) {
    super(message);
  }
}
