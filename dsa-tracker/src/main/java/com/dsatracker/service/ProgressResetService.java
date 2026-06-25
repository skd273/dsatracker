package com.dsatracker.service;

import com.dsatracker.model.Topic;
import com.dsatracker.model.TopicStatus;
import com.dsatracker.repository.ProblemRepository;
import com.dsatracker.repository.TopicRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProgressResetService {

    private final EntityManager entityManager;
    private final TopicRepository topicRepository;

    public ProgressResetService(EntityManager entityManager, TopicRepository topicRepository) {
        this.entityManager = entityManager;
        this.topicRepository = topicRepository;
    }

    @Transactional
    public void resetAllProgress() {
        entityManager.createQuery("UPDATE Problem p SET p.solved = false, p.solvedAt = null")
                .executeUpdate();
        entityManager.createQuery("UPDATE Problem p SET p.revisionNotes = null WHERE p.revisionNotes IS NOT NULL")
                .executeUpdate();

        for (Topic topic : topicRepository.findAll()) {
            topic.setProblemsSolved(0);
            topic.setStatus(TopicStatus.NOT_STARTED);
            topicRepository.save(topic);
        }
    }
}
