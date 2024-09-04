package com.example.ssenotification.controller;

import com.example.ssenotification.data.NotificationDto;
import com.example.ssenotification.data.UserDto;
import com.example.ssenotification.service.NotificationService;
import com.example.ssenotification.service.RefrigeratorUserService;
import com.example.ssenotification.service.SubscribeUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;

@RestController
// @CrossOrigin(origins = {"http://localhost:8080", "https://api.icebuckwheat.kro.kr"}, allowCredentials = "true")
@RequestMapping("/api/notification")
public class AlertController {

    private final NotificationService notificationService;
    private final RefrigeratorUserService refrigeratorUserService;
    private final SubscribeUserService subscribeUserService;

    @Autowired
    public AlertController(NotificationService notificationService,
                           RefrigeratorUserService refrigeratorUserService,
                           SubscribeUserService subscribeUserService) {
        this.notificationService = notificationService;
        this.refrigeratorUserService = refrigeratorUserService;
        this.subscribeUserService = subscribeUserService;
    }

    // 로그인한 사용자의 번호로 SSE 서버에 실시간 알림 구독
    @GetMapping("/subscribe/{userId}")
    public SseEmitter subscribe(@PathVariable String userId)
    {
        notificationService.printActiveSubscriptions();

        return notificationService.subscribe(userId);
    }

    // 로그인한 사용자에게 전송된 알림 조회
    @GetMapping("/getNotification/{userId}")
    public ResponseEntity<List<NotificationDto>> getNotifications(@PathVariable String userId)
    {
        List<NotificationDto> notifications = notificationService.getAllNotificationsForUser(userId);

        return ResponseEntity.ok(notifications);
    }

    // 로그인한 사용자가 읽지 않은 알림 존재 여부 확인해서 true, false 전달
    @GetMapping("/hasUnread/{userId}")
    public ResponseEntity<Boolean> hasUnreadNotifications(@PathVariable String userId)
    {
        boolean hasUnread = notificationService.hasUnreadNotifications(userId);

        return ResponseEntity.ok(hasUnread);
    }

    // notificationId에 해당하는 알림의 alarmcheck를 true로 변경
    @PostMapping("/markAsRead/{alert_id}")
    public ResponseEntity<Void> markAsRead(@PathVariable int alert_id)
    {
        notificationService.markAsRead(alert_id);

        return ResponseEntity.ok().build();
    }

    // notificationId에 해당하는 알림 삭제 처리
    @DeleteMapping("/delete/{alert_id}")
    public ResponseEntity<Void> deleteNotification(@PathVariable int alert_id)
    {
        notificationService.deleteNotification(alert_id);

        return ResponseEntity.ok().build();
    }

    // =============================== 기능별 알림 전송 ===============================
    // =============================== 냉장고 알림 ===============================
    //1. 새로운 냉장고 생성 알림 전송
    //냉장고 생성 마스터가 스스로에게 1대1 알림 전송
    @PostMapping("/createRefrigeratorNotification")
    public ResponseEntity<NotificationDto> createRefrigeratorNotification(@RequestParam(value = "sender") String sender)
    {
        NotificationDto notification = notificationService.sendCreateRefrigeratorNotification(sender);

        return ResponseEntity.ok(notification);
    }

    //2. 냉장고에 새로운 구성원 추가
    //현재 냉장고의 모든 구성원에게 1대다 알림 전송
    @PostMapping("/registRefrigeratorUserNotification")
    public ResponseEntity<NotificationDto> registRefrigeratorUserNotification(@RequestBody Map<String, Object> payload)
    {
        // 디버깅 로그
        System.out.println("Payload received: " + payload);

        String sender = payload.get("sender").toString();//이벤트를 발생시킨 나 자신
        String senderrefri = (String) payload.get("senderrefri");

        NotificationDto notification = notificationService.sendRegistRefrigeratorUserNotification(sender, senderrefri);

        return ResponseEntity.ok(notification);
    }

    //4. 냉장고 삭제
    //현재 냉장고의 모든 구성원에게 1대다 알림 전송
    @PostMapping("/deleteRefrigeratorNotification")
    public ResponseEntity<NotificationDto> deleteRefrigeratorNotification(@RequestBody Map<String, Object> payload) {
        // 디버깅 로그
        System.out.println("Payload received: " + payload);

        String sender = payload.get("sender").toString();//이벤트를 발생시킨 나 자신
        String senderrefri = (String) payload.get("senderrefri");

        NotificationDto notification = notificationService.sendDeleteRefrigeratorNotification(sender, senderrefri);

        return ResponseEntity.ok(notification);
    }


    //3. 냉장고 정보 수정
    //현재 냉장고의 모든 구성원에게 1대다 알림 전송
    @PostMapping("/editRefrigeratorNotification")
    public ResponseEntity<NotificationDto> editRefrigeratorNotification(@RequestBody Map<String, Object> payload)
    {
        // 디버깅 로그
        System.out.println("Payload received: " + payload);

        String sender = payload.get("sender").toString();//이벤트를 발생시킨 나 자신
        List<String> senderrefris = (List<String>) payload.get("senderrefri");

        NotificationDto savedNotification = null;
        for (String senderrefri : senderrefris) {
            NotificationDto notification = notificationService.sendEditRefrigeratorNotification(sender, senderrefri);
            savedNotification = notification;
        }

        return ResponseEntity.ok(savedNotification);
    }

    //5. 냉장고 채팅방 새로운 채팅 발생
    //현재 냉장고의 모든 구성원에게 1대다 알림 전송
    @PostMapping("/newChatting")
    public ResponseEntity<NotificationDto> newChattingNotification(@RequestBody Map<String, Object> payload) {
        // 디버깅 로그
        System.out.println("Payload received: " + payload);

        String sender = payload.get("sender").toString();//이벤트를 발생시킨 나 자신
        String senderrefri = (String) payload.get("senderrefri");

        NotificationDto notification = notificationService.sendNewChattingNotification(sender, senderrefri);

        return ResponseEntity.ok(notification);
    }

    //6. 냉장고 채팅방 새로운 공지 발생
    //현재 냉장고의 모든 구성원에게 1대다 알림 전송
    @PostMapping("/newChattingMaster")
    public ResponseEntity<NotificationDto> newChattingMasterNotification(@RequestBody Map<String, Object> payload) {
        // 디버깅 로그
        System.out.println("Payload received: " + payload);

        String sender = payload.get("sender").toString();//이벤트를 발생시킨 나 자신
        String senderrefri = (String) payload.get("senderrefri");

        NotificationDto notification = notificationService.sendNewChattingMasterNotification(sender, senderrefri);

        return ResponseEntity.ok(notification);
    }

    //7. 유저 삭제 알림
    //현재 냉장고의 모든 구성원에게 1대다 알림 전송
    @PostMapping("/deleteUserFromRefrigerator")
    public ResponseEntity<NotificationDto> deleteUserFromRefrigeratorNotification(@RequestBody Map<String, Object> payload)
    {
        // 디버깅 로그
        System.out.println("Payload received: " + payload);

        String sender = payload.get("sender").toString();//이벤트를 발생시킨 나 자신
        String senderrefri = (String) payload.get("senderrefri");

        NotificationDto notification = notificationService.sendDeleteUserFromRefrigeratorNotification(sender, senderrefri);

        return ResponseEntity.ok(notification);
    }

    // =============================== 커뮤니티 알림 ===============================
    //1. 좋아요 클릭
    @PostMapping("/checkLikeNotification")
    public ResponseEntity<NotificationDto> checkLikeNotification(@RequestParam String sender, @RequestParam String receiver, @RequestParam String recipeposting) {
        // senderId와 receiverId를 서비스로 전달하여 알림 생성
        NotificationDto notification = notificationService.sendCheckLikeNotification(sender, receiver, recipeposting);

        return ResponseEntity.ok(notification);
    }

    //2. 댓글 작성
    @PostMapping("/writeReply")
    public ResponseEntity<NotificationDto> writeReply(@RequestParam String sender, @RequestParam String receiver, @RequestParam String recipeposting) {

        NotificationDto notification = notificationService.sendWriteReplyNotification(sender, receiver, recipeposting);

        return ResponseEntity.ok(notification);
    }

    //3. 포스팅 작성 // 1대다 전송
    @PostMapping("/writePosting")
    public ResponseEntity<NotificationDto> writePosting(@RequestParam String sender)
    {
        NotificationDto notification = notificationService.sendWritePostingNotification(sender);

        return ResponseEntity.ok(notification);
    }

    //4. 구독
    @PostMapping("/subscribeUser")
    public ResponseEntity<NotificationDto> subscribeUser(@RequestParam String sender, @RequestParam String receiver) {

        NotificationDto notification = notificationService.sendSubscribeNotification(sender, receiver);

        return ResponseEntity.ok(notification);
    }

    //5. 방송 시작 알림 / 1대다 알림
    @PostMapping("/startBroadcasting")
    public ResponseEntity<NotificationDto> startBroadcasting(@RequestParam String sender)
    {
        NotificationDto notification = notificationService.sendStartBroadcastingNotification(sender);

        return ResponseEntity.ok(notification);
    }

    @GetMapping("/find/refrigeratorUsers")
    public List<String> GetRefrigeratorUser(@RequestParam String refrigerator_id) {
        return refrigeratorUserService.getUserIdsByRefrigeratorId(refrigerator_id);
    }

    @GetMapping("/find/subscribeUsers")
    public List<String> GetSubscribeUser(@RequestParam String userId) {
        return subscribeUserService.getUserIdsBySubScribeUser(userId);
    }

}
