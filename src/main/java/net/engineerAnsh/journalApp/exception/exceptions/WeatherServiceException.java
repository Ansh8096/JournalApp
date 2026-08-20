package net.engineerAnsh.journalApp.exception.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class WeatherServiceException extends RuntimeException {

    private final HttpStatus status;

    public WeatherServiceException(
            String message,
            HttpStatus status
    ) {
        super(message);
        this.status = status;
    }

    public WeatherServiceException(
            String message,
            HttpStatus status,
            Throwable cause
    ) {
        super(message, cause);
        this.status = status;
    }
}