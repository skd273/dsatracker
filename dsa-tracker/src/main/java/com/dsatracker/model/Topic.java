package com.dsatracker.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "topics")
public class Topic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(length = 1000)
    private String description;

    @Column(length = 2000)
    private String keyPatterns;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TopicStatus status = TopicStatus.NOT_STARTED;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty = Difficulty.MEDIUM;

    @Column(nullable = false)
    private int targetProblems = 15;

    @Column(nullable = false)
    private int problemsSolved = 0;

    @Column(length = 2000)
    private String notes;

    @Column(nullable = false)
    private int displayOrder;

    @OneToMany(mappedBy = "topic")
    @OrderBy("difficulty ASC, displayOrder ASC")
    private List<Problem> problems = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKeyPatterns() {
        return keyPatterns;
    }

    public void setKeyPatterns(String keyPatterns) {
        this.keyPatterns = keyPatterns;
    }

    public TopicStatus getStatus() {
        return status;
    }

    public void setStatus(TopicStatus status) {
        this.status = status;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public int getTargetProblems() {
        return targetProblems;
    }

    public void setTargetProblems(int targetProblems) {
        this.targetProblems = targetProblems;
    }

    public int getProblemsSolved() {
        return problemsSolved;
    }

    public void setProblemsSolved(int problemsSolved) {
        this.problemsSolved = problemsSolved;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public int getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public List<Problem> getProblems() {
        return problems;
    }

    public void setProblems(List<Problem> problems) {
        this.problems = problems;
    }

    public int getProgressPercent() {
        if (targetProblems <= 0) {
            return 0;
        }
        return Math.min(100, (problemsSolved * 100) / targetProblems);
    }

    public List<Problem> getEasyProblems() {
        List<Problem> easy = new ArrayList<>();
        for (Problem problem : problems) {
            if (problem.getDifficulty() == Difficulty.EASY) {
                easy.add(problem);
            }
        }
        return easy;
    }

    public List<Problem> getMediumProblems() {
        List<Problem> medium = new ArrayList<>();
        for (Problem problem : problems) {
            if (problem.getDifficulty() == Difficulty.MEDIUM) {
                medium.add(problem);
            }
        }
        return medium;
    }

    public List<Problem> getHardProblems() {
        List<Problem> hard = new ArrayList<>();
        for (Problem problem : problems) {
            if (problem.getDifficulty() == Difficulty.HARD) {
                hard.add(problem);
            }
        }
        return hard;
    }
}
