package com.example.ssenotification.controller;

import com.example.ssenotification.pythonrunner.PythonRunner;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
public class RecommendationController {

    @PostMapping("/matrixFactorizationRecommendations")
    public ResponseEntity<String> getRecommendations(@RequestBody Map<String, Object> payload) {
        try {
            // JSON 파싱을 통해 user_id 추출
            String userId = (String) payload.get("userId");
            String decodedUserId =  URLDecoder.decode(userId, StandardCharsets.UTF_8);
            //System.out.println(userId);
            //System.out.println(decodedUserId);

            PythonRunner runner = new PythonRunner();
            // Python 스크립트 실행 (userId를 인자로 전달)
            String recommendations = runner.runPythonScript(decodedUserId);
            // 성공적인 실행 후 결과 반환 (JSON 데이터)
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            // Python 스크립트 실행 중 오류가 발생한 경우
            return ResponseEntity.status(500).body("Python script execution failed: " + e.getMessage());
        }
    }
}
