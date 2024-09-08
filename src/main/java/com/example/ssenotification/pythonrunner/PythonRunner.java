package com.example.ssenotification.pythonrunner;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PythonRunner {
    public String runPythonScript(String userDataJson) {
    String result = "";
    try {
        // Python 스크립트 실행
        ProcessBuilder pb = new ProcessBuilder("python3", "/path/to/your/script.py", userDataJson);
        Process process = pb.start();

        // 스크립트의 출력 결과 읽기
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        StringBuilder output = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            output.append(line);
        }
        result = output.toString();

        // 프로세스 종료 대기
        process.waitFor();
    } catch (Exception e) {
        e.printStackTrace();
    }
    return result;
}
}
