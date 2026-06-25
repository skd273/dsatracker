package com.dsatracker.repository;

import com.dsatracker.model.Topic;
import com.dsatracker.model.TopicStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TopicRepository extends JpaRepository<Topic, Long> {

    @EntityGraph(attributePaths = "problems")
    List<Topic> findAllByOrderByDisplayOrderAsc();

    long countByStatus(TopicStatus status);
}
