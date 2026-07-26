package net.engineerAnsh.journalApp.Dto.journals;

import lombok.*;
import net.engineerAnsh.journalApp.enums.Mood;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalSummaryDto {

    private String id;

    private String title;

    private String contentPreview;

    private Mood mood;

    private boolean favorite;

    private LocalDateTime createdAt;

    private String coverImageUrl;

}