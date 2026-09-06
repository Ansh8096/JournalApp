package net.engineerAnsh.journalApp.Dto.journals;

import lombok.*;
import net.engineerAnsh.journalApp.enums.JournalStatus;
import net.engineerAnsh.journalApp.enums.Mood;
import java.time.LocalDateTime;
import java.util.List;

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

    private String coverImageUrl;

    private JournalStatus status;

    private List<String> tags;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private LocalDateTime publishedAt;
}