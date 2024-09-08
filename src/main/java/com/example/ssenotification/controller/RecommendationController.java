package com.example.ssenotification.controller;

import com.example.ssenotification.pythonrunner.PythonRunner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @PostMapping
    public ResponseEntity<String> getRecommendations(@RequestBody String userDataJson) {
        try {
            // JSON 파싱을 통해 user_id 추출
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(userDataJson);
            String userId = jsonNode.get("user_id").asText();

            PythonRunner runner = new PythonRunner();
            // Python 스크립트 실행 (userId를 인자로 전달)
            String recommendations = runner.runPythonScript(userId);
            // 성공적인 실행 후 결과 반환 (JSON 데이터)
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            // Python 스크립트 실행 중 오류가 발생한 경우
            return ResponseEntity.status(500).body("Python script execution failed: " + e.getMessage());
        }
    }
}
