package com.dsatracker.controller;

import com.dsatracker.dto.DashboardStats;
import com.dsatracker.dto.TopicUpdateRequest;
import com.dsatracker.model.Topic;
import com.dsatracker.service.TopicService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
public class TopicApiController {

    private final TopicService topicService;

    public TopicApiController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping
    public List<Topic> getAllTopics() {
        return topicService.getAllTopics();
    }

    @GetMapping("/stats")
    public DashboardStats getStats() {
        return topicService.getDashboardStats();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Topic> updateTopic(
            @PathVariable Long id,
            @Valid @RequestBody TopicUpdateRequest request) {
        Topic updated = topicService.updateTopic(id, request);
        return ResponseEntity.ok(updated);
    }
}
