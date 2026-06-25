package com.dsatracker.dto;

import com.dsatracker.model.Difficulty;
import com.dsatracker.model.TopicStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class TopicUpdateRequest {

    @NotNull
    private TopicStatus status;

    @Min(1)
    private int targetProblems;

    private String notes;

    @NotNull
    private Difficulty difficulty;

    public TopicStatus getStatus() {
        return status;
    }

    public void setStatus(TopicStatus status) {
        this.status = status;
    }

    public int getTargetProblems() {
        return targetProblems;
    }

    public void setTargetProblems(int targetProblems) {
        this.targetProblems = targetProblems;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }
}
