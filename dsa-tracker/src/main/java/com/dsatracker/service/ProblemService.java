package com.dsatracker.service;

import com.dsatracker.model.Problem;
import com.dsatracker.model.Topic;
import com.dsatracker.model.TopicStatus;
import com.dsatracker.repository.ProblemRepository;
import com.dsatracker.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final TopicRepository topicRepository;

    public ProblemService(ProblemRepository problemRepository, TopicRepository topicRepository) {
        this.problemRepository = problemRepository;
        this.topicRepository = topicRepository;
    }

    @Transactional(readOnly = true)
    public Problem getProblem(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + id));
        problem.getTopic().getName();
        return problem;
    }

    @Transactional
    public Problem toggleSolved(Long id) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + id));

        problem.setSolved(!problem.isSolved());
        if (problem.isSolved()) {
            problem.setSolvedAt(LocalDateTime.now());
        } else {
            problem.setSolvedAt(null);
        }
        problemRepository.save(problem);
        syncTopicProgress(problem.getTopic());

        return problem;
    }

    @Transactional
    public Problem updateRevisionNotes(Long id, String revisionNotes) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found: " + id));
        problem.setRevisionNotes(revisionNotes);
        return problemRepository.save(problem);
    }

    private void syncTopicProgress(Topic topic) {
        int solvedCount = (int) problemRepository.countByTopicAndSolvedTrue(topic);
        topic.setProblemsSolved(solvedCount);

        if (solvedCount >= topic.getTargetProblems()) {
            topic.setStatus(TopicStatus.COMPLETED);
        } else if (solvedCount > 0) {
            topic.setStatus(TopicStatus.IN_PROGRESS);
        } else {
            topic.setStatus(TopicStatus.NOT_STARTED);
        }

        topicRepository.save(topic);
    }
}
