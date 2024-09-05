package com.example.ssenotification.service;

import com.example.ssenotification.data.FoodRemainingDays;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class FoodRemainingDaysService {

    private final NotificationService notificationService;

    public FoodRemainingDaysService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "report-food", groupId = "consumer")
    public void listen(String record) {
        System.out.println(record);

        // 중괄호 제거
        record = record.replace("{", "").replace("}", "");

        // 쉼표로 구분하여 각 key=value 형식의 문자열을 배열로 분리
        String[] keyValuePairs = record.split(", ");

        // 값을 저장할 변수
        String remainingDay = "";
        String refrigeratorId = "";
        String id = "";

        // 각 key=value 쌍을 순회하며 값 추출
        for (String pair : keyValuePairs) {
            String[] entry = pair.split("=");
            String key = entry[0];
            String value = entry[1];

            // 키 값에 따라 적절한 변수에 저장
            if (key.equals("remainingDays")) {
                remainingDay = value;
            } else if (key.equals("refrigerator_id")) {
                refrigeratorId = value;
            } else if (key.equals("id")) {
                id = value;
            }
        }

        // 알림 전송
        notificationService.sendFoodExpirationNotification(id, refrigeratorId, remainingDay);
    }
}
