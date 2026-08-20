package net.engineerAnsh.journalApp.validation;

import lombok.RequiredArgsConstructor;
import net.engineerAnsh.journalApp.Config.common.JournalProperties;
import net.engineerAnsh.journalApp.exception.exceptions.BadRequestException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TagValidator {

    private final JournalProperties journalProperties;

    public List<String> normalize(List<String> tags) {

        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>();
        }

        Map<String, String> normalizedTags = new LinkedHashMap<>(); // we use LinkedHashmap here, because it maintains the user order.

        for (String tag : tags) {

            if (tag == null) {
                continue;
            }

            String trimmedTag = tag.trim();

            if (trimmedTag.isBlank()) {
                continue;
            }

            if (trimmedTag.length() >
                    journalProperties.getTags().getMaxLength()) {

                throw new BadRequestException(
                        String.format(
                                "Tag '%s' exceeds the maximum length of %d characters.",
                                trimmedTag,
                                journalProperties.getTags().getMaxLength()
                        )
                );
            }

            normalizedTags.putIfAbsent(
                    trimmedTag.toLowerCase(Locale.ROOT),
                    trimmedTag
            );
        }

        if (normalizedTags.size() >
                journalProperties.getTags().getMaxCount()) {

            throw new BadRequestException(
                    String.format(
                            "A journal can contain at most %d tags.",
                            journalProperties.getTags().getMaxCount()
                    )
            );
        }

        return new ArrayList<>(normalizedTags.values());
    }
}