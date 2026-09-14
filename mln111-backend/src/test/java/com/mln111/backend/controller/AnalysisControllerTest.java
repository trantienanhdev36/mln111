package com.mln111.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mln111.backend.model.AnalysisRequest;
import com.mln111.backend.service.GeminiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class AnalysisControllerTest {

    private MockMvc mockMvc;
    private GeminiService geminiService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        geminiService = mock(GeminiService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AnalysisController(geminiService)).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void analyzeRequestReturnsAnalysisResult() throws Exception {
        AnalysisRequest request = new AnalysisRequest();
        request.setTitle("Bài viết kiểm thử");
        request.setUrl("https://example.com");
        request.setContent("Nội dung kiểm thử");
        when(geminiService.analyze(any(AnalysisRequest.class)))
                .thenReturn("<p>Kết quả phân tích mẫu</p>");

        mockMvc.perform(post("/analyze")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(view().name("result"))
                .andExpect(model().attribute("title", "Bài viết kiểm thử"))
                .andExpect(model().attribute("url", "https://example.com"))
                .andExpect(model().attribute("analysis", "<p>Kết quả phân tích mẫu</p>"));
    }
}
