package com.example.ssenotification.service;

import com.example.ssenotification.data.NotificationDto;
import com.example.ssenotification.repository.NotificationDaoInter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationService {

    @Autowired
    private NotificationDaoInter notificationDaoInter;

    @Autowired
    private RefrigeratorUserService refrigeratorUserService;

    @Autowired
    private SubscribeUserService subscribeUserService;

    // userEmitters map 생성 : userid를 키로 하고 SseEmitter 객체를 저장하는 컨테이너
    private final Map<String, SseEmitter> userEmitters = new ConcurrentHashMap<>();

    // 현재 로그인한 사용자가 서버에 SSE 구독
    public SseEmitter subscribe(String userId) {
        // 기존에 동일한 사용자 ID로 구독된 SSE를 찾고 종료
        SseEmitter existingEmitter = userEmitters.get(userId);
        if (existingEmitter != null) {
            existingEmitter.complete();  // 기존 SSE 구독 종료
            //System.out.println("기존 SSE 구독 종료: userId=" + userId);
        }

        // 새로운 SSE 구독 생성
        SseEmitter emitter = new SseEmitter(120_000L);//120초마다
        userEmitters.put(userId, emitter);

        // SSE 구독 성공 로그 출력
        //System.out.println("SSE 구독 성공: userId=" + userId);

        // Emitter 연결 해제
        emitter.onCompletion(() -> {
            userEmitters.remove(userId);
            //System.out.println("SSE 연결 종료: userId=" + userId);
        });

        emitter.onTimeout(() -> {
            userEmitters.remove(userId);
            //System.out.println("SSE 연결 타임아웃: userId=" + userId);
            emitter.complete();
        });

        emitter.onError(e -> {
            userEmitters.remove(userId);
            //System.out.println("SSE 연결 오류: userId=" + userId);
            emitter.completeWithError(e);
        });

        // 더미 이벤트 전송 시 재연결 시간 설정
        try {
            emitter.send(SseEmitter.event()
                    .name("dummyEvent")
                    .data("연결이 성공적으로 이루어졌습니다.")
                    .reconnectTime(1000));  // 재연결 대기 시간 1초 (1000ms)
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 로그인 시 저장된 읽지 않은 알림 전송
        List<NotificationDto> notifications = getAllNotificationsForUser(userId);
        notifications.forEach(notification -> {
            try {
                sendEvent(userId, notification); // 이벤트 전송
            } catch (Exception e) {
                emitter.completeWithError(e);
                userEmitters.remove(userId);
            }
        });

        return emitter;
    }

    // 현재 구독 중인 모든 사용자의 ID를 출력하는 메서드
    public void printActiveSubscriptions() {
        System.out.println("\n현재 SSE 구독 중인 사용자 리스트:");
        userEmitters.keySet().forEach(userId -> {
            System.out.println("- userId: " + userId);
        });
        System.out.println();
    }

    // userId 사용자의 모든 알림 가져오기
    public List<NotificationDto> getAllNotificationsForUser(String userId)//디코딩된 userId
    {
        return notificationDaoInter.findByReceiver(userId);
    }

    // userId의 안 읽은 알림 존재 여부 확인
    public boolean hasUnreadNotifications(String userId)//디코딩된 userId
    {
        return notificationDaoInter.existsByReceiverAndAlertcheckFalse(userId);
    }

    // notificationId인 알림 읽음 처리 // alarmcheck 값 수정
    public void markAsRead(int alert_id)
    {
        NotificationDto notification = notificationDaoInter.findById(alert_id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid notification ID"));
        notification.setAlertcheck(true);
        notificationDaoInter.save(notification);
    }

    // notificationId 알림 삭제 처리
    public void deleteNotification(int alert_id)
    {
        notificationDaoInter.deleteById(alert_id);
    }

    // SSE를 통한 실시간 알림 전송
    private void sendEvent(String receiver, NotificationDto notification)
    {
        SseEmitter emitter = userEmitters.get(receiver);

        if (emitter != null)
        {
            try
            {
                // JSON 형식으로 변환 후 전송
                String jsonNotification = new ObjectMapper().writeValueAsString(notification); //ObjectMapper를 통해 JSON형태로 변환
                emitter.send(SseEmitter.event().data(jsonNotification));
            }
            catch (IOException e)
            {
                emitter.completeWithError(e);
                userEmitters.remove(receiver);//안보내졌다고 연결을 끊을 필요가 있나.
            }
        }
    }

    // 새로운 냉장고 생성 알림 전송 // 1대1 알림
    public NotificationDto sendCreateRefrigeratorNotification(String sender, String memo)//sender가 인코딩 되어 있네
    {
        NotificationDto notification = new NotificationDto();
        notification.setSender(sender);
        notification.setReceiver(sender);
        notification.setAlerttype("냉장고 생성");
        notification.setMemo(memo);

        NotificationDto savedNotification = notificationDaoInter.save(notification);//DB에 저장
        sendEvent(sender, savedNotification);//전송

        return savedNotification;
    }

    // 다대일 알림 전송 로직
    public NotificationDto sendMultiUserNotification(String sender, List<String> receivers, String alertType, String senderrefri, String memo) {
        NotificationDto lastSavedNotification = null;

        for (String receiver : receivers) {
            try {
                NotificationDto notification = new NotificationDto();
                notification.setSender(sender);
                notification.setReceiver(receiver);
                notification.setAlerttype(alertType);
                notification.setSenderrefri(senderrefri);
                notification.setMemo(memo);

                // DB에 알림 저장
                lastSavedNotification = notificationDaoInter.save(notification);

                // 알림을 실시간으로 전송
                sendEvent(receiver, lastSavedNotification);

            } catch (Exception e) {
                System.err.println("알림 전송 중 오류 발생: " + e.getMessage());
                // 계속해서 다음 사용자에게 알림을 전송
            }
        }

        return lastSavedNotification;
    }

    // 냉장고에 새로운 구성원 등록 알림 //1대다 알림
    public NotificationDto sendRegistRefrigeratorUserNotification(String sender, String senderrefri, String memo) {
        List<String> receivers = refrigeratorUserService.getUserIdsByRefrigeratorId(senderrefri);
        return sendMultiUserNotification(sender, receivers, "냉장고 등록", senderrefri, memo);
    }

    // 냉장고 삭제 알림 //1대다 알림
    public NotificationDto sendDeleteRefrigeratorNotification(String sender, String senderrefri, String memo) {
        List<String> receivers = refrigeratorUserService.getUserIdsByRefrigeratorId(senderrefri);
        NotificationDto lastSavedNotification = null;

        for (String receiver : receivers)
        {
            NotificationDto notification = new NotificationDto();
            notification.setSender(sender);
            notification.setReceiver(receiver);
            notification.setAlerttype("냉장고 삭제");
            //notification.setSenderrefri(senderrefri);
            notification.setMemo(memo);

            // DB에 알림 저장
            lastSavedNotification = notificationDaoInter.save(notification);

            // 실시간으로 알림 전송
            sendEvent(receiver, lastSavedNotification);
        }
        //deleteAllNotificationsByRefrigerator(senderrefri);

        return lastSavedNotification; // 마지막으로 생성된 알림 반환
    }

    // senderrefri와 관련된 모든 알림 삭제
    private void deleteAllNotificationsByRefrigerator(String senderrefri) {
        notificationDaoInter.deleteBySenderrefri(senderrefri);
    }

    // 냉장고 정보 수정 알림 // 1대다 알림
    public NotificationDto sendEditRefrigeratorNotification(String sender, String senderrefri, String memo) {
        List<String> receivers = refrigeratorUserService.getUserIdsByRefrigeratorId(senderrefri);
        return sendMultiUserNotification(sender, receivers, "냉장고 수정", senderrefri, memo);
    }

    //냉장고 구성원 삭제 알림 // 1대다 알림
    public NotificationDto sendDeleteUserFromRefrigeratorNotification(String sender, String senderrefri, String memo) {
        List<String> receivers = refrigeratorUserService.getUserIdsByRefrigeratorId(senderrefri);
        receivers.add(memo);
        return sendMultiUserNotification(sender, receivers, "구성원 삭제", senderrefri, memo);
    }

    //냉장고 구성원 채팅 알림 // 1대다 알림
    public NotificationDto sendNewChattingNotification(String sender, String senderrefri, String memo) {
        List<String> receivers = refrigeratorUserService.getUserIdsByRefrigeratorId(senderrefri);
        return sendMultiUserNotification(sender, receivers, "채팅", senderrefri, memo);
    }

    //냉장고 구성원 채팅 공지 알림 // 1대다 알림
    public NotificationDto sendNewChattingMasterNotification(String sender, String senderrefri, String memo) {
        List<String> receivers = refrigeratorUserService.getUserIdsByRefrigeratorId(senderrefri);
        return sendMultiUserNotification(sender, receivers, "채팅방 공지", senderrefri, memo);
    }

    //유통기한 알림 //1대 다 알림
    public NotificationDto sendFoodExpirationNotification(String food_id, String refrigeratorId, String remainingDay) {
        // 냉장고의 모든 사용자 목록을 가져옴
        List<String> receivers = refrigeratorUserService.getUserIdsByRefrigeratorId(refrigeratorId);
        return sendMultiUserNotification(food_id, receivers, "유통기한 임박", refrigeratorId, remainingDay);
    }

    //=========== 커뮤니티 알림 ===============
    // 좋아요 클릭 //1대1알림
    public NotificationDto sendCheckLikeNotification(String sender, String receiver, String recipeposting, String memo)
    {
        NotificationDto notification = new NotificationDto();
        notification.setSender(sender);
        notification.setReceiver(receiver);
        notification.setRecipeposting(recipeposting);
        notification.setAlerttype("좋아요");
        notification.setMemo(memo);

        NotificationDto savedNotification = notificationDaoInter.save(notification);

        sendEvent(receiver, savedNotification);

        return savedNotification;
    }

    // 댓글 알림 전송 //1대1알림
    public NotificationDto sendWriteReplyNotification(String sender, String receiver, String recipeposting, String memo)
    {
        NotificationDto notification = new NotificationDto();
        notification.setSender(sender);
        notification.setReceiver(receiver);
        notification.setRecipeposting(recipeposting);
        notification.setAlerttype("댓글 작성");
        notification.setMemo(memo);

        NotificationDto savedNotification = notificationDaoInter.save(notification);

        sendEvent(receiver, savedNotification);

        return savedNotification;
    }

    // 구독 알림 전송 //1대1알림
    public NotificationDto sendSubscribeNotification(String sender, String receiver, String memo)
    {
        NotificationDto notification = new NotificationDto();
        notification.setSender(sender);
        notification.setReceiver(receiver);
        notification.setAlerttype("구독");
        notification.setMemo(memo);

        NotificationDto savedNotification = notificationDaoInter.save(notification);

        sendEvent(receiver, savedNotification);

        return savedNotification;
    }

    //포스팅 작성 //1대다 알림
    public NotificationDto sendWritePostingNotification(String sender, String recipeposting, String memo)
    {
        List<String> receivers = subscribeUserService.getUserIdsBySubScribeUser(sender);
        NotificationDto savedNotification = null;

        for (String receiver : receivers)
        {
            NotificationDto notification = new NotificationDto();

            notification.setSender(sender);
            notification.setReceiver(receiver);
            notification.setRecipeposting(recipeposting);
            notification.setAlerttype("포스팅 작성");
            notification.setMemo(memo);
            savedNotification = notificationDaoInter.save(notification);
            sendEvent(receiver, savedNotification);
        }

        return savedNotification; // 마지막으로 생성된 알림 반환
    }

    // posting_id와 관련된 알림 삭제 (삭제 알림은 제외)
    public void deleteAllNotificationsByPostingId(String recipeposting) {
        notificationDaoInter.deleteByRecipeposting(recipeposting);
    }

    //방송 시작 //1대다 알림
    public NotificationDto sendStartBroadcastingNotification(String sender, String memo)
    {
        List<String> receivers = subscribeUserService.getUserIdsBySubScribeUser(sender);
        NotificationDto savedNotification = null;

        for (String receiver : receivers)
        {
            NotificationDto notification = new NotificationDto();

            notification.setSender(sender);
            notification.setReceiver(receiver);
            notification.setAlerttype("방송 시작");
            notification.setMemo(memo);
            savedNotification = notificationDaoInter.save(notification);
            sendEvent(receiver, savedNotification);
        }

        return savedNotification; // 마지막으로 생성된 알림 반환
    }

}
