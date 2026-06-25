package com.dsatracker.controller;

import com.dsatracker.service.ProgressResetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/progress")
public class ProgressApiController {

    private final ProgressResetService progressResetService;

    public ProgressApiController(ProgressResetService progressResetService) {
        this.progressResetService = progressResetService;
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> resetProgress() {
        progressResetService.resetAllProgress();
        return ResponseEntity.ok(Map.of("message", "All progress has been reset"));
    }
}
