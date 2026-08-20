package net.engineerAnsh.journalApp.helper;

import net.engineerAnsh.journalApp.Dto.journals.JournalStatisticsResponseDto;
import net.engineerAnsh.journalApp.Entity.Journal;
import net.engineerAnsh.journalApp.enums.Mood;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class JournalStatisticsCalculator {

    private record MoodStatistics(
            Mood mood,
            double percentage
    ) {}

    private long calculateFavoriteJournals(
            List<Journal> journals
    ) {

        return journals.stream()
                .filter(Journal::isFavorite)
                .count();
    }

    private long calculateJournalsThisMonth(
            List<Journal> journals
    ) {

        LocalDate today = LocalDate.now();

        return journals.stream()
                .filter(journal -> {

                    LocalDate created =
                            journal.getCreatedAt()
                                    .toLocalDate();

                    return created.getYear() == today.getYear()
                            && created.getMonth() == today.getMonth();

                })
                .count();
    }

    private MoodStatistics calculateMostCommonMood(
            List<Journal> journals
    ) {

        if (journals.isEmpty()) {

            return new MoodStatistics(
                    null,
                    0
            );
        }

        EnumMap<Mood, Long> moodCounts =
                new EnumMap<>(Mood.class);

        for (Journal journal : journals) {

            moodCounts.merge(
                    journal.getMood(),
                    1L,
                    Long::sum
            );
        }

        Map.Entry<Mood, Long> mostCommon =
                moodCounts.entrySet()
                        .stream()
                        .max(Map.Entry.comparingByValue())
                        .orElseThrow();

        double percentage =
                Math.round(
                        (mostCommon.getValue() * 1000.0)
                                / journals.size()
                ) / 10.0;

        return new MoodStatistics(
                mostCommon.getKey(),
                percentage
        );
    }

    private int calculateCurrentStreak(
            List<Journal> journals
    ) {

        Set<LocalDate> journalDates =
                journals.stream()
                        .map(journal ->
                                journal.getCreatedAt()
                                        .toLocalDate())
                        .collect(Collectors.toSet());

        LocalDate current = LocalDate.now();

        if (!journalDates.contains(current)) {
            current = current.minusDays(1);
        }

        int streak = 0;

        while (journalDates.contains(current)) {

            streak++;

            current = current.minusDays(1);
        }

        return streak;
    }

    public JournalStatisticsResponseDto calculate(
            List<Journal> journals
    ) {

        long totalJournals = journals.size();

        long favoriteJournals = calculateFavoriteJournals(journals);

        long journalsThisMonth = calculateJournalsThisMonth(journals);

        MoodStatistics moodStatistics =
                calculateMostCommonMood(journals);

        int currentStreak =
                calculateCurrentStreak(journals);

        return JournalStatisticsResponseDto.builder()
                .totalJournals(totalJournals)
                .favoriteJournals(favoriteJournals)
                .mostCommonMood(
                        moodStatistics.mood()
                )
                .mostCommonMoodPercentage(
                        moodStatistics.percentage()
                )
                .journalsThisMonth(journalsThisMonth)
                .currentStreak(currentStreak)
                .build();
    }
}