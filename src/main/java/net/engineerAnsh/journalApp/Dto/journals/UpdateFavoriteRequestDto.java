package net.engineerAnsh.journalApp.Dto.journals;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateFavoriteRequestDto {

    @NotNull
    private Boolean favorite;
}