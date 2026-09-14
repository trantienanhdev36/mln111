package com.mln111.backend.controller;

import com.mln111.backend.model.AnalysisRequest;
import com.mln111.backend.service.GeminiService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@CrossOrigin(origins = "*")
public class AnalysisController {

    private final GeminiService geminiService;

    public AnalysisController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/analyze")
    public String analyze(@RequestBody AnalysisRequest request, Model model) {
        try {
            String analysis = geminiService.analyze(request);
            model.addAttribute("title", request.getTitle());
            model.addAttribute("url", request.getUrl());
            model.addAttribute("analysis", analysis);
            return "result";
        } catch (IllegalStateException exception) {
            model.addAttribute("title", request.getTitle());
            model.addAttribute("url", request.getUrl());
            model.addAttribute("error", exception.getMessage());
            return "result";
        }
    }
}
