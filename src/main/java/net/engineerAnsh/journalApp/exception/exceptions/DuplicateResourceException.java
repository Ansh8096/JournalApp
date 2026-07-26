package net.engineerAnsh.journalApp.exception.exceptions;

public class DuplicateResourceException extends RuntimeException {

    private final String field;

    public DuplicateResourceException(
            String field,
            String message
    ) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}