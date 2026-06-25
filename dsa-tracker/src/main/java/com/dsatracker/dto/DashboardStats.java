package com.dsatracker.dto;

public class DashboardStats {

    private final long totalTopics;
    private final long completedTopics;
    private final long inProgressTopics;
    private final long notStartedTopics;
    private final int totalProblemsSolved;
    private final int totalTargetProblems;
    private final int overallProgressPercent;

    public DashboardStats(long totalTopics, long completedTopics, long inProgressTopics,
                          long notStartedTopics, int totalProblemsSolved, int totalTargetProblems) {
        this.totalTopics = totalTopics;
        this.completedTopics = completedTopics;
        this.inProgressTopics = inProgressTopics;
        this.notStartedTopics = notStartedTopics;
        this.totalProblemsSolved = totalProblemsSolved;
        this.totalTargetProblems = totalTargetProblems;
        this.overallProgressPercent = totalTargetProblems > 0
                ? Math.min(100, (totalProblemsSolved * 100) / totalTargetProblems)
                : 0;
    }

    public long getTotalTopics() {
        return totalTopics;
    }

    public long getCompletedTopics() {
        return completedTopics;
    }

    public long getInProgressTopics() {
        return inProgressTopics;
    }

    public long getNotStartedTopics() {
        return notStartedTopics;
    }

    public int getTotalProblemsSolved() {
        return totalProblemsSolved;
    }

    public int getTotalTargetProblems() {
        return totalTargetProblems;
    }

    public int getOverallProgressPercent() {
        return overallProgressPercent;
    }
}
