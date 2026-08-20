package net.engineerAnsh.journalApp.Dto.journals;

import lombok.*;
import net.engineerAnsh.journalApp.enums.Mood;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JournalStatisticsResponseDto {

    private long totalJournals;

    private long favoriteJournals;

    private Mood mostCommonMood;

    private double mostCommonMoodPercentage;

    private long journalsThisMonth;

    private int currentStreak;
}