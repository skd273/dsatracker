package com.dsatracker.controller;

import com.dsatracker.dto.RevisionNotesRequest;
import com.dsatracker.model.Problem;
import com.dsatracker.service.ProblemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/problems")
public class ProblemApiController {

    private final ProblemService problemService;

    public ProblemApiController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @PutMapping("/{id}/toggle")
    public ResponseEntity<Problem> toggleSolved(@PathVariable Long id) {
        return ResponseEntity.ok(problemService.toggleSolved(id));
    }

    @PutMapping("/{id}/revision-notes")
    public ResponseEntity<Problem> updateRevisionNotes(
            @PathVariable Long id,
            @RequestBody RevisionNotesRequest request) {
        return ResponseEntity.ok(problemService.updateRevisionNotes(id, request.getRevisionNotes()));
    }
}
