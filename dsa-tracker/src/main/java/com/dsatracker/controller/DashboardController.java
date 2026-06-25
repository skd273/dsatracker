package com.dsatracker.controller;

import com.dsatracker.dto.ActivityStats;
import com.dsatracker.dto.DashboardStats;
import com.dsatracker.dto.ProblemOfTheDay;
import com.dsatracker.model.Topic;
import com.dsatracker.service.ActivityService;
import com.dsatracker.service.ProblemOfTheDayService;
import com.dsatracker.service.TopicService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final TopicService topicService;
    private final ActivityService activityService;
    private final ProblemOfTheDayService problemOfTheDayService;

    public DashboardController(TopicService topicService,
                               ActivityService activityService,
                               ProblemOfTheDayService problemOfTheDayService) {
        this.topicService = topicService;
        this.activityService = activityService;
        this.problemOfTheDayService = problemOfTheDayService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        List<Topic> topics = topicService.getAllTopics();
        DashboardStats stats = topicService.getDashboardStats();
        ActivityStats activity = activityService.getActivityStats();
        ProblemOfTheDay potd = problemOfTheDayService.getToday();

        Map<String, List<Topic>> grouped = topics.stream()
                .collect(Collectors.groupingBy(
                        Topic::getCategory,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        model.addAttribute("stats", stats);
        model.addAttribute("activity", activity);
        model.addAttribute("potd", potd);
        model.addAttribute("groupedTopics", grouped);
        model.addAttribute("topics", topics);
        return "index";
    }
}
