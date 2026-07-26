package net.engineerAnsh.journalApp.Dto.journals;

import jakarta.validation.constraints.Size;
import lombok.*;
import net.engineerAnsh.journalApp.enums.Mood;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateJournalRequestDto {

    @Size(max = 100)
    private String title;

    @Size(max = 10000)
    private String content;

    private Mood mood;
}