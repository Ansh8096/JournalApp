package net.engineerAnsh.journalApp.criteria;

import lombok.*;
import net.engineerAnsh.journalApp.enums.Mood;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalSearchCriteria {

    private String query;

    private Mood mood;

    private Boolean favorite; // If we use primitive boolean, we lose the "don't filter" state.

    private String tag;

    private LocalDate from;

    private LocalDate to;

}
