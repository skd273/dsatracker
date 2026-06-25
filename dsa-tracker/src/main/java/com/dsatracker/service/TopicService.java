package com.dsatracker.service;

import com.dsatracker.dto.DashboardStats;
import com.dsatracker.dto.TopicUpdateRequest;
import com.dsatracker.model.Topic;
import com.dsatracker.model.TopicStatus;
import com.dsatracker.repository.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TopicService {

    private final TopicRepository topicRepository;

    public TopicService(TopicRepository topicRepository) {
        this.topicRepository = topicRepository;
    }

    public List<Topic> getAllTopics() {
        return topicRepository.findAllByOrderByDisplayOrderAsc();
    }

    public Topic getTopic(Long id) {
        return topicRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Topic not found: " + id));
    }

    @Transactional
    public Topic updateTopic(Long id, TopicUpdateRequest request) {
        Topic topic = getTopic(id);
        topic.setStatus(request.getStatus());
        topic.setTargetProblems(request.getTargetProblems());
        topic.setNotes(request.getNotes());
        topic.setDifficulty(request.getDifficulty());

        int solvedCount = (int) topic.getProblems().stream().filter(p -> p.isSolved()).count();
        topic.setProblemsSolved(solvedCount);

        if (solvedCount >= request.getTargetProblems()
                && request.getStatus() != TopicStatus.COMPLETED) {
            topic.setStatus(TopicStatus.COMPLETED);
        } else if (solvedCount > 0
                && request.getStatus() == TopicStatus.NOT_STARTED) {
            topic.setStatus(TopicStatus.IN_PROGRESS);
        }

        return topicRepository.save(topic);
    }

    public DashboardStats getDashboardStats() {
        List<Topic> topics = getAllTopics();
        long completed = topicRepository.countByStatus(TopicStatus.COMPLETED);
        long inProgress = topicRepository.countByStatus(TopicStatus.IN_PROGRESS);
        long notStarted = topicRepository.countByStatus(TopicStatus.NOT_STARTED);

        int solved = topics.stream().mapToInt(Topic::getProblemsSolved).sum();
        int target = topics.stream().mapToInt(Topic::getTargetProblems).sum();

        return new DashboardStats(
                topics.size(),
                completed,
                inProgress,
                notStarted,
                solved,
                target
        );
    }
}
