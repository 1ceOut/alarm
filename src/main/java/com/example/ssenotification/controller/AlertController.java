package com.example.ssenotification.controller;

import com.example.ssenotification.data.NotificationDto;
import com.example.ssenotification.service.NotificationService;
import com.example.ssenotification.service.RefrigeratorUserService;
import com.example.ssenotification.service.SubscribeUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@RestController
//@CrossOrigin(origins = {"http://localhost:8080", "https://api.icebuckwheat.kro.kr"}, allowCredentials = "true")
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
    public SseEmitter subscribe(@PathVariable String userId) {
        //System.out.println("SSE subscribe userID : " +  userId);
        String decodedUserId = URLDecoder.decode(userId, StandardCharsets.UTF_8);
        SseEmitter emitter = notificationService.subscribe(decodedUserId);
        //System.out.println("decodedUserId : " + decodedUserId );
        //SseEmitter emitter = notificationService.subscribe(userId);
        //notificationService.printActiveSubscriptions();
        return emitter;
    }

    // 로그인한 사용자에게 전송된 알림 조회
    @GetMapping("/getNotification/{userId}")
    public ResponseEntity<List<NotificationDto>> getNotifications(@PathVariable String userId)//인코딩 데이터로 받아서
    {
        String decodedUserId = URLDecoder.decode(userId, StandardCharsets.UTF_8);
        List<NotificationDto> notifications = notificationService.getAllNotificationsForUser(decodedUserId);//디코딩 데이터로 전송
        //List<NotificationDto> notifications = notificationService.getAllNotificationsForUser(userId);

        return ResponseEntity.ok(notifications);
    }

    // 로그인한 사용자가 읽지 않은 알림 존재 여부 확인해서 true, false 전달
    @GetMapping("/hasUnread/{userId}")
    public ResponseEntity<Boolean> hasUnreadNotifications(@PathVariable String userId)//인코딩 데이터로 받아서
    {
        String decodedUserId = URLDecoder.decode(userId, StandardCharsets.UTF_8);
        boolean hasUnread = notificationService.hasUnreadNotifications(decodedUserId);//디코딩 데이터로 전송
        //boolean hasUnread = notificationService.hasUnreadNotifications(userId);//디코딩 데이터로 전송

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
    // 새로운 냉장고 생성 알림 전송
    //냉장고 생성 마스터가 스스로에게 1대1 알림 전송
    @PostMapping("/createRefrigeratorNotification")
    public ResponseEntity<NotificationDto> createRefrigeratorNotification(@RequestBody Map<String, Object> payload)//인코딩 데이터로 받아서
    {
        String sender = payload.get("sender").toString();//이벤트를 발생시킨 나 자신
        String decodedSender = URLDecoder.decode(sender, StandardCharsets.UTF_8);//디코딩데이터로 변환
        String memo = (String) payload.get("memo");

        NotificationDto notification = notificationService.sendCreateRefrigeratorNotification(decodedSender, memo);
        //NotificationDto notification = notificationService.sendCreateRefrigeratorNotification(sender, memo);

        return ResponseEntity.ok(notification);
    }

    // 냉장고에 새로운 구성원 추가
    //현재 냉장고의 모든 구성원에게 1대다 알림 전송
    @PostMapping("/registRefrigeratorUserNotification")
    public ResponseEntity<NotificationDto> registRefrigeratorUserNotification(@RequestBody Map<String, Object> payload)
    {
        // 디버깅 로그
        System.out.println("Payload received: " + payload);

        String sender = payload.get("sender").toString();//이벤트를 발생시킨 나 자신
        String decodedSender = URLDecoder.decode(sender, StandardCharsets.UTF_8);//디코딩데이터로 변환
        String senderrefri = (String) payload.get("senderrefri");
        String memo = (String) payload.get("memo");

        NotificationDto notification = notificationService.sendRegistRefrigeratorUserNotification(decodedSender, senderrefri, memo);
        //NotificationDto notification = notificationService.sendRegistRefrigeratorUserNotification(sender, senderrefri, memo);

        return ResponseEntity.ok(notification);
    }

    // 냉장고 삭제
    //현재 냉장고의 모든 구성원에게 1대다 알림 전송
    @PostMapping("/deleteRefrigeratorNotification")
    public ResponseEntity<NotificationDto> deleteRefrigeratorNotification(@RequestBody Map<String, Object> payload) {
        // 디버깅 로그
        System.out.println("Payload received: " + payload);

        String sender = payload.get("sender").toString();//이벤트를 발생시킨 나 자신
        String decodedSender = URLDecoder.decode(sender, StandardCharsets.UTF_8);//디코딩데이터로 변환
        String senderrefri = (String) payload.get("senderrefri");
        String memo = (String) payload.get("memo");

        NotificationDto notification = notificationService.sendDeleteRefrigeratorNotification(decodedSender, senderrefri, memo);
        //NotificationDto notification = notificationService.sendDeleteRefrigeratorNotification(sender, senderrefri, memo);

        return ResponseEntity.ok(notification);
    }

    // 냉장고 정보 수정
    //현재 냉장고의 모든 구성원에게 1대다 알림 전송
    @PostMapping("/editRefrigeratorNotification")
    public ResponseEntity<NotificationDto> editRefrigeratorNotification(@RequestBody Map<String, Object> payload)
    {
        // 디버깅 로그
        System.out.println("Payload received: " + payload);

        String sender = payload.get("sender").toString();//이벤트를 발생시킨 나 자신
        String decodedSender = URLDecoder.decode(sender, StandardCharsets.UTF_8);//디코딩데이터로 변환
        String senderrefri = (String) payload.get("senderrefri");
        String memo = (String) payload.get("memo");

        NotificationDto notification = notificationService.sendEditRefrigeratorNotification(decodedSender, senderrefri, memo);
        //NotificationDto notification = notificationService.sendEditRefrigeratorNotification(sender, senderrefri, memo);

        return ResponseEntity.ok(notification);
    }

    // 냉장고 채팅방 새로운 채팅 발생
    //현재 냉장고의 모든 구성원에게 1대다 알림 전송
    @PostMapping("/newChatting")
    public ResponseEntity<NotificationDto> newChattingNotification(@RequestBody Map<String, Object> payload) {
        // 디버깅 로그
        System.out.println("Payload received: " + payload);

        String sender = payload.get("sender").toString();//이벤트를 발생시킨 나 자신
        String decodedSender = URLDecoder.decode(sender, StandardCharsets.UTF_8);//디코딩데이터로 변환
        String senderrefri = (String) payload.get("senderrefri");
        String memo = (String) payload.get("memo");

        NotificationDto notification = notificationService.sendNewChattingNotification(decodedSender, senderrefri, memo);
        //NotificationDto notification = notificationService.sendNewChattingNotification(sender, senderrefri, memo);

        return ResponseEntity.ok(notification);
    }

    //6. 냉장고 채팅방 새로운 공지 발생
    //현재 냉장고의 모든 구성원에게 1대다 알림 전송
    @PostMapping("/newChattingMaster")
    public ResponseEntity<NotificationDto> newChattingMasterNotification(@RequestBody Map<String, Object> payload) {
        // 디버깅 로그
        System.out.println("Payload received: " + payload);
        String sender = payload.get("sender").toString();//이벤트를 발생시킨 나 자신
        String decodedSender = URLDecoder.decode(sender, StandardCharsets.UTF_8);//디코딩데이터로 변환
        String senderrefri = (String) payload.get("senderrefri");
        String memo = (String) payload.get("memo");

        NotificationDto notification = notificationService.sendNewChattingMasterNotification(decodedSender, senderrefri, memo);
        //NotificationDto notification = notificationService.sendNewChattingMasterNotification(sender, senderrefri, memo);

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
        String decodedSender = URLDecoder.decode(sender, StandardCharsets.UTF_8);//디코딩데이터로 변환
        String senderrefri = (String) payload.get("senderrefri");
        String memo = (String) payload.get("memo");
        String docodedMemo = URLDecoder.decode(memo, StandardCharsets.UTF_8);

        NotificationDto notification = notificationService.sendDeleteUserFromRefrigeratorNotification(decodedSender, senderrefri, docodedMemo);
        //NotificationDto notification = notificationService.sendDeleteUserFromRefrigeratorNotification(sender, senderrefri, memo);

        return ResponseEntity.ok(notification);
    }

    // =============================== 커뮤니티 알림 ===============================
    //1. 좋아요 클릭
    @PostMapping("/checkLikeNotification")
    public ResponseEntity<NotificationDto> checkLikeNotification(@RequestBody Map<String, Object> payload) {
        String sender = payload.get("sender").toString();
        String receiver = payload.get("receiver").toString();
        String decodedSender = URLDecoder.decode(sender, StandardCharsets.UTF_8);
        String decodedReceiver = URLDecoder.decode(receiver, StandardCharsets.UTF_8);
        String recipeposting = (String) payload.get("recipeposting");
        String memo = (String) payload.get("memo");

        NotificationDto notification = notificationService.sendCheckLikeNotification(decodedSender, decodedReceiver, recipeposting, memo);
        //NotificationDto notification = notificationService.sendCheckLikeNotification(sender, receiver, recipeposting, memo);

        return ResponseEntity.ok(notification);
    }

    //2. 댓글 작성
    @PostMapping("/writeReply")
    public ResponseEntity<NotificationDto> writeReply(@RequestBody Map<String, Object> payload) {
        String sender = payload.get("sender").toString();
        String receiver = payload.get("receiver").toString();
        String decodedSender = URLDecoder.decode(sender, StandardCharsets.UTF_8);
        String decodedReceiver = URLDecoder.decode(receiver, StandardCharsets.UTF_8);
        String recipeposting = (String) payload.get("recipeposting");
        String memo = (String) payload.get("memo");

        NotificationDto notification = notificationService.sendCheckLikeNotification(decodedSender, decodedReceiver, recipeposting, memo);
        //NotificationDto notification = notificationService.sendWriteReplyNotification(sender, receiver, recipeposting, memo);

        return ResponseEntity.ok(notification);
    }

    //3. 포스팅 작성 // 1대다 전송
    @PostMapping("/writePosting")
    public ResponseEntity<NotificationDto> writePosting(@RequestBody Map<String, Object> payload)
    {
        System.out.println("Payload received: " + payload);

        String sender = payload.get("sender").toString();
        String decodedSender = URLDecoder.decode(sender, StandardCharsets.UTF_8);
        String recipeposting = (String) payload.get("recipeposting");
        String memo = (String) payload.get("memo");

        NotificationDto notification = notificationService.sendWritePostingNotification(decodedSender, recipeposting, memo);
        //NotificationDto notification = notificationService.sendWritePostingNotification(sender, recipeposting, memo);

        return ResponseEntity.ok(notification);
    }

    //4. 구독
    @PostMapping("/subscribeUser")
    public ResponseEntity<NotificationDto> subscribeUser(@RequestBody Map<String, Object> payload) {
        String sender = payload.get("sender").toString();
        String receiver = payload.get("receiver").toString();
        String decodedSender = URLDecoder.decode(sender, StandardCharsets.UTF_8);
        String decodedReceiver = URLDecoder.decode(receiver, StandardCharsets.UTF_8);
        String memo = (String) payload.get("memo");

        NotificationDto notification = notificationService.sendSubscribeNotification(decodedSender, decodedReceiver, memo);
        //NotificationDto notification = notificationService.sendSubscribeNotification(sender, receiver, memo);

        return ResponseEntity.ok(notification);
    }

    //5. 방송 시작 알림 / 1대다 알림
    @PostMapping("/startBroadcasting")
    public ResponseEntity<NotificationDto> startBroadcasting(@RequestBody Map<String, Object> payload)
    {
        String sender = payload.get("sender").toString();
        String decodedSender = URLDecoder.decode(sender, StandardCharsets.UTF_8);
        String memo = (String) payload.get("memo");

        NotificationDto notification = notificationService.sendStartBroadcastingNotification(decodedSender, memo);
        //NotificationDto notification = notificationService.sendStartBroadcastingNotification(sender, memo);

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

    @GetMapping("/find/refriNameByRefriId")
    public String getRefriName(@RequestParam String refrigerator_id) {
        return refrigeratorUserService.getRefrigeratorName(refrigerator_id); // 수정된 부분
    }

}
