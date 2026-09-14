package com.mln111.backend.controller;

import com.mln111.backend.model.AnalysisRequest;
import com.mln111.backend.service.GeminiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@CrossOrigin(origins = "*")
public class AnalysisController {

    private static final Logger log = LoggerFactory.getLogger(AnalysisController.class);

    private final GeminiService geminiService;

    public AnalysisController(GeminiService geminiService) {
        this.geminiService = geminiService;
    }

    @PostMapping("/analyze")
    public String analyze(@RequestBody AnalysisRequest request, Model model) {
        log.info("Nhận request analyze: title={}, url={}", request.getTitle(), request.getUrl());

        try {
            String analysis = geminiService.analyze(request);
            log.info("Gọi Gemini xong, trả về kết quả phân tích, độ dài={}", analysis != null ? analysis.length() : 0);

            model.addAttribute("title", request.getTitle());
            model.addAttribute("url", request.getUrl());
            model.addAttribute("analysis", analysis);
            log.info("Trả về view 'result' với phân tích thành công");
            return "result";
        } catch (IllegalStateException exception) {
            log.error("Lỗi khi xử lý request analyze: title={}, url={}, error={}",
                    request.getTitle(), request.getUrl(), exception.getMessage(), exception);

            model.addAttribute("title", request.getTitle());
            model.addAttribute("url", request.getUrl());
            model.addAttribute("error", exception.getMessage());
            log.info("Trả về view 'result' với lỗi");
            return "result";
        }
    }
}
