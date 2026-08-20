package net.engineerAnsh.journalApp.Utils;

import org.springframework.stereotype.Component;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;

@Component
public class FileNameGenerator {

    private static final int MAX_FILE_NAME_LENGTH = 100;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public String journalPdfFileName(String title) {

        return journalPdfFileName(
                title,
                null
        );
    }

    public String journalPdfFileName(
            String title,
            java.time.LocalDateTime createdAt
    ) {

        String fileName = normalize(title);

        if (createdAt != null) {

            fileName += "_"
                    + createdAt.format(DATE_FORMATTER);
        }

        return fileName + ".pdf";
    }

    private String normalize(String value) {

        if (value == null || value.isBlank()) {
            return "journal";
        }

        String normalized = Normalizer.normalize(
                value,
                Normalizer.Form.NFD
        );

        normalized = normalized.replaceAll("\\p{M}", "");

        normalized = normalized.replaceAll("[^a-zA-Z0-9\\s-]", "");

        normalized = normalized.trim();

        normalized = normalized.replaceAll("\\s+", "-");

        normalized = normalized.replaceAll("-+", "-");

        if (normalized.length() > MAX_FILE_NAME_LENGTH) {
            normalized =
                    normalized.substring(
                            0,
                            MAX_FILE_NAME_LENGTH
                    );
        }

        return normalized;
    }
}