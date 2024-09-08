package com.example.ssenotification.pythonrunner;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PythonRunner {
    public String runPythonScript(String userId) throws Exception {
        String result = "";
        ProcessBuilder pb = new ProcessBuilder(
                "python",
                "src/main/resources/pythonrunner/matrixFactorizationRecipeRecommendation.py",
                userId
        );

        pb.redirectErrorStream(true); // 표준 오류를 표준 출력으로 합침
        Process process = pb.start();

        // 스크립트의 출력 결과 읽기
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }

        // 프로세스 종료 대기
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Python script exited with error code: " + exitCode);
        }

        result = output.toString();
        return result;
    }
}
