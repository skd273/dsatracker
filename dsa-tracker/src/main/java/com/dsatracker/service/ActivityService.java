package com.dsatracker.service;

import com.dsatracker.dto.ActivityStats;
import com.dsatracker.model.Problem;
import com.dsatracker.repository.ProblemRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Service
public class ActivityService {

    private static final int HEATMAP_WEEKS = 26;

    private final ProblemRepository problemRepository;

    public ActivityService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    public ActivityStats getActivityStats() {
        List<Problem> solved = problemRepository.findBySolvedTrueAndSolvedAtNotNull();
        Map<LocalDate, Integer> counts = new HashMap<>();
        for (Problem problem : solved) {
            LocalDate day = problem.getSolvedAt().toLocalDate();
            counts.merge(day, 1, Integer::sum);
        }

        LocalDate end = LocalDate.now();
        LocalDate start = end.minusWeeks(HEATMAP_WEEKS).plusDays(1);
        int maxCount = counts.values().stream().mapToInt(Integer::intValue).max().orElse(0);

        List<ActivityStats.HeatmapDay> heatmap = start.datesUntil(end.plusDays(1))
                .map(day -> new ActivityStats.HeatmapDay(day.toString(), counts.getOrDefault(day, 0)))
                .toList();

        TreeSet<LocalDate> activeDays = new TreeSet<>(counts.keySet());
        return new ActivityStats(heatmap, currentStreak(activeDays, end), longestStreak(activeDays), maxCount);
    }

    private int currentStreak(TreeSet<LocalDate> activeDays, LocalDate today) {
        if (activeDays.isEmpty()) {
            return 0;
        }
        int streak = 0;
        LocalDate cursor = today;
        while (activeDays.contains(cursor)) {
            streak++;
            cursor = cursor.minusDays(1);
        }
        if (streak == 0 && activeDays.contains(today.minusDays(1))) {
            cursor = today.minusDays(1);
            while (activeDays.contains(cursor)) {
                streak++;
                cursor = cursor.minusDays(1);
            }
        }
        return streak;
    }

    private int longestStreak(TreeSet<LocalDate> activeDays) {
        if (activeDays.isEmpty()) {
            return 0;
        }
        int longest = 1;
        int current = 1;
        LocalDate prev = null;
        for (LocalDate day : activeDays) {
            if (prev != null) {
                if (prev.plusDays(1).equals(day)) {
                    current++;
                } else {
                    current = 1;
                }
            }
            longest = Math.max(longest, current);
            prev = day;
        }
        return longest;
    }
}
