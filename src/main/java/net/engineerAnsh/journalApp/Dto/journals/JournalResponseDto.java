package net.engineerAnsh.journalApp.Dto.journals;

import net.engineerAnsh.journalApp.enums.JournalStatus;
import net.engineerAnsh.journalApp.enums.Mood;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalResponseDto {

    private String id;

    private String title;

    private String content;

    private Mood mood;

    private boolean favorite;

    private String coverImageUrl;

    @Builder.Default
    private List<JournalImageResponseDto> images = List.of();

    private List<String> tags;

    private JournalStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}