package com.dsatracker.repository;

import com.dsatracker.model.Problem;
import com.dsatracker.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProblemRepository extends JpaRepository<Problem, Long> {

    long countByTopicAndSolvedTrue(Topic topic);

    List<Problem> findBySolvedTrueAndSolvedAtNotNull();

    List<Problem> findBySolvedFalse();

    @Query("SELECT COUNT(p) FROM Problem p WHERE p.url IS NOT NULL AND TRIM(p.url) <> ''")
    long countWithExternalLinks();
}
