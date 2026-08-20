package net.engineerAnsh.journalApp.Dto.journals;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import net.engineerAnsh.journalApp.enums.Mood;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateJournalRequestDto {

    @NotBlank(message = "Title is required")
    @Size(max = 120)
    private String title;

    @NotBlank(message = "Content is required")
    @Size(max = 10000)
    private String content;

    @NotNull(message = "Mood is required")
    private Mood mood;

    private List<String> tags;
}