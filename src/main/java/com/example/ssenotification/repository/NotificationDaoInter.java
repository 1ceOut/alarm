package com.example.ssenotification.repository;

import com.example.ssenotification.data.NotificationDto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationDaoInter extends JpaRepository<NotificationDto, Integer> {

    // 특정 사용자의 모든 알림 가져오기
    List<NotificationDto> findByReceiver(String receiver);

    // 한번도 확인 안 한 알림 존재 여부 확인
    boolean existsByReceiverAndAlertcheckFalse(String receiver);

}