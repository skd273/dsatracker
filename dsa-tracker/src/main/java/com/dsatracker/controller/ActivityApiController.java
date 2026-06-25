package com.dsatracker.controller;

import com.dsatracker.dto.ActivityStats;
import com.dsatracker.service.ActivityService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/activity")
public class ActivityApiController {

    private final ActivityService activityService;

    public ActivityApiController(ActivityService activityService) {
        this.activityService = activityService;
    }

    @GetMapping
    public ActivityStats getActivity() {
        return activityService.getActivityStats();
    }
}
