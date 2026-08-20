package net.engineerAnsh.journalApp.exception.exceptions;

public class PdfGenerationException extends RuntimeException {

    public PdfGenerationException(
            String message,
            Throwable cause
    ) {
        super(
                message,
                cause
        );
    }
}
