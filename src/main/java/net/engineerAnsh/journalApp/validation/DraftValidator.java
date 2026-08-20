package net.engineerAnsh.journalApp.validation;

import net.engineerAnsh.journalApp.enums.Mood;
import net.engineerAnsh.journalApp.exception.exceptions.BadRequestException;
import org.springframework.stereotype.Component;

@Component
public class DraftValidator {

    public void validateForPublish(
            String title,
            String content,
            Mood mood
    ) {

        if (title == null || title.isBlank()) {

            throw new BadRequestException(
                    "Journal title is required before publishing."
            );
        }

        if (content == null || content.isBlank()) {

            throw new BadRequestException(
                    "Journal content is required before publishing."
            );
        }

        if (mood == null) {

            throw new BadRequestException(
                    "Journal mood is required before publishing."
            );
        }
    }
}