package net.engineerAnsh.journalApp.Dto.user;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequestDto {

    @Size(max = 100)
    private String city;

    private boolean sentimentAnalysisEnabled;
}
