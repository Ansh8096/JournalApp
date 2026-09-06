package net.engineerAnsh.journalApp.mapper;

import net.engineerAnsh.journalApp.Dto.journals.JournalImageResponseDto;
import net.engineerAnsh.journalApp.Dto.journals.JournalResponseDto;
import net.engineerAnsh.journalApp.Dto.journals.JournalSummaryDto;
import net.engineerAnsh.journalApp.Entity.Journal;
import net.engineerAnsh.journalApp.model.JournalImage;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class JournalMapper {

    private JournalImageResponseDto toImageResponseDto(JournalImage image) {

        if (image == null) {
            return null;
        }

        return JournalImageResponseDto.builder()
                .imageUrl(image.getImageUrl())
                .publicId(image.getPublicId())
                .build();
    }

    private List<JournalImageResponseDto> toImageResponseDtos(
            List<JournalImage> images
    ) {

        if (images == null || images.isEmpty()) {
            return List.of();
        }

        return images.stream()
                .map(this::toImageResponseDto)
                .toList();
    }

    public JournalSummaryDto toSummaryDto(Journal journal) {

        if (journal == null) return null;
        return JournalSummaryDto.builder()
                .id(journal.getId().toHexString())
                .title(journal.getTitle())
                .contentPreview(createPreview(journal.getContent()))
                .mood(journal.getMood())
                .favorite(journal.isFavorite())
                .coverImageUrl(journal.getCoverImageUrl())
                .tags(journal.getTags())
                .status(journal.getStatus())
                .createdAt(journal.getCreatedAt())
                .updatedAt(journal.getUpdatedAt())
                .publishedAt(journal.getPublishedAt())
                .build();
    }

    public JournalResponseDto toResponseDto(Journal journal) {

        if (journal == null) return null;
        return JournalResponseDto.builder()
                .id(journal.getId().toHexString())
                .title(journal.getTitle())
                .content(journal.getContent())
                .mood(journal.getMood())
                .favorite(journal.isFavorite())
                .coverImageUrl(journal.getCoverImageUrl())
                .images(toImageResponseDtos(journal.getImages()))
                .tags(journal.getTags())
                .status(journal.getStatus())
                .createdAt(journal.getCreatedAt())
                .updatedAt(journal.getUpdatedAt())
                .publishedAt(journal.getPublishedAt())
                .build();
    }

    private String createPreview(String content) {

        if (content == null || content.isBlank()) {
            return "";
        }

        if (content.length() <= 120) {
            return content;
        }

        return content.substring(0, 120) + "...";
    }

}
