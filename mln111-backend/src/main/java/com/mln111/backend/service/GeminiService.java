package com.mln111.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mln111.backend.model.AnalysisRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
public class GeminiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public GeminiService(
            RestTemplate restTemplate,
            ObjectMapper objectMapper,
            @Value("${gemini.api.key}") String apiKey,
            @Value("${gemini.api.model:gemini-2.0-flash}") String model) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    public String analyze(AnalysisRequest request) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Chưa cấu hình GEMINI_API_KEY cho backend.");
        }

        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;
        ObjectNode payload = objectMapper.createObjectNode();
        ArrayNode contents = payload.putArray("contents");
        ObjectNode content = contents.addObject();
        ArrayNode parts = content.putArray("parts");
        parts.addObject().put("text", buildPrompt(request));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity;
        try {
            entity = new HttpEntity<>(objectMapper.writeValueAsString(payload), headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    endpoint,
                    HttpMethod.POST,
                    entity,
                    String.class);
            return extractText(response.getBody());
        } catch (RestClientException exception) {
            throw new IllegalStateException("Không thể kết nối đến Gemini: " + exception.getMessage(), exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Không thể xử lý phản hồi từ Gemini.", exception);
        }
    }

    private String buildPrompt(AnalysisRequest request) {
        return "Bạn là trợ giảng môn Triết học Mác-Lênin (MLN111) tại Việt Nam. "
                + "Hãy phân tích nội dung bài viết dưới đây bằng tiếng Việt, trung thực với nguồn được cung cấp "
                + "và ưu tiên lập luận rõ ràng, có dẫn chứng từ chính nội dung. Trình bày dưới dạng HTML an toàn, "
                + "chỉ sử dụng các thẻ h2, h3, p, ul, ol, li, strong, em và blockquote. "
                + "Không tạo thẻ html, head, body, script hoặc thuộc tính style. "
                + "Cấu trúc gồm: tóm tắt, các luận điểm liên hệ với phép biện chứng duy vật hoặc chủ nghĩa duy vật lịch sử, "
                + "ý nghĩa phương pháp luận, và câu hỏi ôn tập. Nếu nội dung không liên quan, hãy nói rõ giới hạn phân tích.\n\n"
                + "Tiêu đề: " + safeValue(request.getTitle()) + "\n"
                + "URL: " + safeValue(request.getUrl()) + "\n"
                + "Nội dung:\n" + safeValue(request.getContent());
    }

    private String extractText(String responseBody) throws Exception {
        if (responseBody == null || responseBody.isBlank()) {
            throw new IllegalStateException("Gemini trả về phản hồi trống.");
        }

        JsonNode response = objectMapper.readTree(responseBody);
        JsonNode text = response.path("candidates").path(0).path("content").path("parts").path(0).path("text");
        if (text.isMissingNode() || text.asText().isBlank()) {
            String errorMessage = response.path("error").path("message").asText("Gemini không trả về nội dung phân tích.");
            throw new IllegalStateException(errorMessage);
        }
        return text.asText();
    }

    private String safeValue(String value) {
        return value == null ? "(không có)" : value;
    }
}
