package net.engineerAnsh.journalApp.helper;

import net.engineerAnsh.journalApp.Dto.journals.JournalStatisticsResponseDto;
import net.engineerAnsh.journalApp.Entity.Journal;
import net.engineerAnsh.journalApp.enums.Mood;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JournalStatisticsCalculator {

    private record MoodStatistics(
            Mood mood,
            double percentage
    ) {
    }

    /**
     * ----------------------------------------
     * FAVORITE JOURNALS
     * ----------------------------------------
     *
     * This statistic is not date-based, so
     * createdAt/publishedAt does not matter.
     */
    private long calculateFavoriteJournals(
            List<Journal> journals
    ) {

        return journals.stream()
                .filter(Journal::isFavorite)
                .count();
    }

    /**
     * ----------------------------------------
     * PUBLICATION DATE
     * ----------------------------------------
     *
     * New published journals have publishedAt.
     *
     * Existing published journals may still
     * have publishedAt == null until STEP 5
     * migration is completed.
     *
     * Therefore, temporarily fall back to
     * createdAt for legacy documents.
     */
    private LocalDate getPublicationDate(
            Journal journal
    ) {

        LocalDateTime publicationTime =
                journal.getPublishedAt();

        if (publicationTime != null) {
            return publicationTime.toLocalDate();
        }

        /*
         * Temporary compatibility fallback for
         * old published journals.
         *
         * STEP 5 will backfill publishedAt and
         * this fallback can then be removed.
         */
        return journal.getCreatedAt() != null
                ? journal.getCreatedAt().toLocalDate()
                : null;
    }

    /**
     * ----------------------------------------
     * JOURNALS THIS MONTH
     * ----------------------------------------
     *
     * This represents journals that became
     * published during the current month.
     */
    private long calculateJournalsThisMonth(
            List<Journal> journals
    ) {

        LocalDate today =
                LocalDate.now();

        return journals.stream()
                .map(this::getPublicationDate)
                .filter(Objects::nonNull)
                .filter(publicationDate ->
                        publicationDate.getYear()
                                == today.getYear()
                                &&
                                publicationDate.getMonth()
                                        == today.getMonth()
                )
                .count();
    }

    /**
     * ----------------------------------------
     * MOST COMMON MOOD
     * ----------------------------------------
     *
     * This statistic is not publication-date
     * based.
     */
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

            if (journal.getMood() == null) {
                continue;
            }

            moodCounts.merge(
                    journal.getMood(),
                    1L,
                    Long::sum
            );
        }

        if (moodCounts.isEmpty()) {
            return new MoodStatistics(
                    null,
                    0
            );
        }

        Map.Entry<Mood, Long> mostCommon =
                moodCounts.entrySet()
                        .stream()
                        .max(
                                Map.Entry.comparingByValue()
                        )
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

    /**
     * ----------------------------------------
     * CURRENT STREAK
     * ----------------------------------------
     *
     * A streak represents consecutive dates on
     * which the user published a journal.
     *
     * Therefore publishedAt is the correct date.
     */
    private int calculateCurrentStreak(
            List<Journal> journals
    ) {

        Set<LocalDate> publicationDates =
                journals.stream()
                        .map(this::getPublicationDate)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

        if (publicationDates.isEmpty()) {
            return 0;
        }

        LocalDate current =
                LocalDate.now();

        /*
         * If nothing was published today,
         * allow the streak to continue from
         * yesterday.
         */
        if (!publicationDates.contains(current)) {
            current =
                    current.minusDays(1);
        }

        int streak = 0;

        while (
                publicationDates.contains(
                        current
                )
        ) {
            streak++;

            current =
                    current.minusDays(1);
        }

        return streak;
    }

    /**
     * ----------------------------------------
     * CALCULATE ALL STATISTICS
     * ----------------------------------------
     */
    public JournalStatisticsResponseDto calculate(
            List<Journal> journals
    ) {

        long totalJournals =
                journals.size();

        long favoriteJournals =
                calculateFavoriteJournals(
                        journals
                );

        long journalsThisMonth =
                calculateJournalsThisMonth(
                        journals
                );

        MoodStatistics moodStatistics =
                calculateMostCommonMood(
                        journals
                );

        int currentStreak =
                calculateCurrentStreak(
                        journals
                );

        return JournalStatisticsResponseDto.builder()
                .totalJournals(
                        totalJournals
                )
                .favoriteJournals(
                        favoriteJournals
                )
                .mostCommonMood(
                        moodStatistics.mood()
                )
                .mostCommonMoodPercentage(
                        moodStatistics.percentage()
                )
                .journalsThisMonth(
                        journalsThisMonth
                )
                .currentStreak(
                        currentStreak
                )
                .build();
    }
}