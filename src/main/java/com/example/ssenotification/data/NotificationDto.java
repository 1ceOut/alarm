package com.example.ssenotification.data;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;

@Entity
@Table(name = "alert")
@Data
public class NotificationDto
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")  // SQL의 alert_id와 매핑
    private int alert_id; // 인덱스 번호

    @Column(name = "alerttype", nullable = false, length = 30)
    private String alerttype; // 알림 타입 EX) 유통기한 임박, 냉장고 추가, 냉장고 삭제, 냉장고 수정, 등

//    @Column(name = "profileimage", length = 100)
//    private String profileimage;

    @Column(name = "sender", length = 100)
    private String sender; // 발신자 ID

//    @Column(name = "sendername", length = 50)
//    private String sendername;

    @Column(name = "senderrefri", length = 100)
    private String senderrefri; // 발신 냉장고 UUID

//    @Column(name = "senderrefriname", length = 50)
//    private String senderrefriname;

    @Column(name = "senderfood", length = 100)
    private String senderfood; // 발신 음식 UUID

    @Column(name = "receiver", length = 100)
    private String receiver; // 수신자 ID

//    @Column(name = "receivername", length = 50)
//    private String receivername;

    @Column(name = "recipeposting", length = 100)
    private String recipeposting; // 레시피 포스팅 UUID

    @Column(name = "alertcheck", nullable = false)
    private boolean alertcheck = false; // 알림 확인 여부 -> 알림 확인을 통한 색상 변경

    @Column(name = "memo", length = 300)
    private String memo;

    @CreationTimestamp
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
    @Column(name = "alertday", nullable = false, updatable = false)
    private Timestamp alertday; // 알림 발생 시간
}
