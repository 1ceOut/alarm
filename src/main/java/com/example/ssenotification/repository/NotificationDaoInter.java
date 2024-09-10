package com.example.ssenotification.repository;

import com.example.ssenotification.data.NotificationDto;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationDaoInter extends JpaRepository<NotificationDto, Integer> {

    // 특정 사용자의 모든 알림 가져오기
    List<NotificationDto> findByReceiver(String receiver);

    // 한번도 확인 안 한 알림 존재 여부 확인
    boolean existsByReceiverAndAlertcheckFalse(String receiver);

    // 특정 냉장고와 관련된 모든 알림 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM NotificationDto n WHERE n.senderrefri = :senderrefri")
    void deleteBySenderrefri(String senderrefri);

    // recipeposting에 해당하는 모든 알림 삭제
    @Modifying
    @Transactional
    @Query("DELETE FROM NotificationDto n WHERE n.recipeposting = :recipeposting")
    void deleteByRecipeposting(String recipeposting);
}