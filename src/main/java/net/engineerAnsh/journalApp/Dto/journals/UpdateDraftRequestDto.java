package net.engineerAnsh.journalApp.Dto.journals;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.engineerAnsh.journalApp.enums.Mood;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateDraftRequestDto implements JournalUpdateRequest{


    @Size(max = 120)
    private String title;

    @Size(max = 10000)
    private String content;

    private Mood mood;

    private List<String> tags;

    private List<String> removeImagePublicIds;
}