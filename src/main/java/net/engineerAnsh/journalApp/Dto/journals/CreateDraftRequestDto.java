package net.engineerAnsh.journalApp.Dto.journals;

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
public class CreateDraftRequestDto {

    private String title;

    private String content;

    private Mood mood;

    private List<String> tags;
}