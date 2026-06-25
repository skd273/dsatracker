package com.dsatracker.model;

public enum Platform {
    LEETCODE("LeetCode"),
    GEEKSFORGEEKS("GeeksforGeeks"),
    CODEFORCES("Codeforces"),
    HACKERRANK("HackerRank"),
    CODESTUDIO("CodeStudio"),
    INTERVIEWBIT("InterviewBit");

    private final String displayName;

    Platform(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
