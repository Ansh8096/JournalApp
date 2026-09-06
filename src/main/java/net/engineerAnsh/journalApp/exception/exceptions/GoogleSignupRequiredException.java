package net.engineerAnsh.journalApp.exception.exceptions;

public class GoogleSignupRequiredException
        extends RuntimeException {

    public GoogleSignupRequiredException(
            String message
    ) {
        super(message);
    }
}
