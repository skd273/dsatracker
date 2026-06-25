package com.dsatracker.dto;

import com.dsatracker.model.Difficulty;
import com.dsatracker.model.Platform;

import java.time.LocalDate;

public record ProblemOfTheDay(
        Long id,
        String title,
        String url,
        Platform platform,
        Difficulty difficulty,
        boolean solved,
        String topicName,
        Long topicId,
        LocalDate date
) {}
