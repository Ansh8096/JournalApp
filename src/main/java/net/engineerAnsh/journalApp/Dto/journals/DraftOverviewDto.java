package net.engineerAnsh.journalApp.Dto.journals;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DraftOverviewDto {

    private long totalDrafts;

    private long updatedToday;

    private long averageDraftAgeDays;
}