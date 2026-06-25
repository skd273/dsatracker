package com.dsatracker.controller;

import com.dsatracker.model.Problem;
import com.dsatracker.service.ProblemService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/problems")
public class ProblemController {

    private final ProblemService problemService;

    public ProblemController(ProblemService problemService) {
        this.problemService = problemService;
    }

    @GetMapping("/{id}")
    public String problemDetail(@PathVariable Long id, Model model) {
        Problem problem = problemService.getProblem(id);
        model.addAttribute("problem", problem);
        return "problem";
    }
}
