package com.dsatracker.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ActivityStats {

    private final List<HeatmapDay> heatmap;
    private final List<List<HeatmapDay>> weeks;
    private final int currentStreak;
    private final int longestStreak;
    private final int maxCount;

    public ActivityStats(List<HeatmapDay> heatmap, int currentStreak, int longestStreak, int maxCount) {
        this.heatmap = heatmap;
        this.weeks = buildWeekGrid(heatmap);
        this.currentStreak = currentStreak;
        this.longestStreak = longestStreak;
        this.maxCount = maxCount;
    }

    public List<HeatmapDay> getHeatmap() {
        return heatmap;
    }

    public List<List<HeatmapDay>> getWeeks() {
        return weeks;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public int getLongestStreak() {
        return longestStreak;
    }

    public int getMaxCount() {
        return maxCount;
    }

    public int levelFor(int count) {
        if (count <= 0 || maxCount <= 0) {
            return 0;
        }
        if (count == 1) {
            return 1;
        }
        if (count <= maxCount / 3) {
            return 2;
        }
        if (count <= (maxCount * 2) / 3) {
            return 3;
        }
        return 4;
    }

    public record HeatmapDay(String date, int count) {}

    private static List<List<HeatmapDay>> buildWeekGrid(List<HeatmapDay> days) {
        if (days.isEmpty()) {
            return List.of();
        }

        java.time.LocalDate start = java.time.LocalDate.parse(days.get(0).date());
        java.time.LocalDate end = java.time.LocalDate.parse(days.get(days.size() - 1).date());
        java.time.LocalDate gridStart = start.minusDays((start.getDayOfWeek().getValue() - 1 + 7) % 7);

        Map<java.time.LocalDate, Integer> countMap = days.stream()
                .collect(Collectors.toMap(d -> java.time.LocalDate.parse(d.date()), HeatmapDay::count));

        List<List<HeatmapDay>> weeks = new ArrayList<>();
        List<HeatmapDay> week = new ArrayList<>();

        for (java.time.LocalDate day = gridStart; !day.isAfter(end); day = day.plusDays(1)) {
            boolean inRange = !day.isBefore(start) && !day.isAfter(end);
            week.add(new HeatmapDay(inRange ? day.toString() : "", inRange ? countMap.getOrDefault(day, 0) : -1));
            if (week.size() == 7) {
                weeks.add(week);
                week = new ArrayList<>();
            }
        }
        if (!week.isEmpty()) {
            while (week.size() < 7) {
                week.add(new HeatmapDay("", -1));
            }
            weeks.add(week);
        }
        return weeks;
    }
}
