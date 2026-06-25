package com.dsatracker.service;

import com.dsatracker.dto.ProblemOfTheDay;
import com.dsatracker.model.Problem;
import com.dsatracker.repository.ProblemRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
public class ProblemOfTheDayService {

    private final ProblemRepository problemRepository;

    public ProblemOfTheDayService(ProblemRepository problemRepository) {
        this.problemRepository = problemRepository;
    }

    @Transactional(readOnly = true)
    public ProblemOfTheDay getToday() {
        LocalDate today = LocalDate.now();
        List<Problem> pool = problemRepository.findAll();
        if (pool.isEmpty()) {
            return null;
        }

        Problem problem = pool.get(new Random(today.toEpochDay()).nextInt(pool.size()));
        return toDto(problem, today);
    }

    private ProblemOfTheDay toDto(Problem problem, LocalDate date) {
        return new ProblemOfTheDay(
                problem.getId(),
                problem.getTitle(),
                problem.getUrl(),
                problem.getPlatform(),
                problem.getDifficulty(),
                problem.isSolved(),
                problem.getTopic().getName(),
                problem.getTopic().getId(),
                date
        );
    }
}
