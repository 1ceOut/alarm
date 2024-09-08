package com.example.ssenotification.controller;

import com.example.ssenotification.pythonrunner.PythonRunner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/recommendations")
public class RecommentdationController {

    @PostMapping
    public ResponseEntity<String> getRecommendations(@RequestBody String userDataJson) {
        try {
            PythonRunner runner = new PythonRunner();
            // Python 스크립트 실행
            String recommendations = runner.runPythonScript(userDataJson);
            // 성공적인 실행 후 결과 반환 (JSON 데이터)
            return ResponseEntity.ok(recommendations);
        } catch (Exception e) {
            // Python 스크립트 실행 중 오류가 발생한 경우
            return ResponseEntity.status(500).body("Python script execution failed: " + e.getMessage());
        }
    }
}
