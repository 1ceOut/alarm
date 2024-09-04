-- --------------------------------------테이블 정의 코드--------------------------------------

-- 알림 테이블
-- 하나로 모든 알림을 책임지게끔...
CREATE TABLE alert (
    alert_id INT AUTO_INCREMENT PRIMARY KEY,
    -- profileimage varchar(100), -- 발신자 프로필 사진
    alerttype varchar(30), -- 알림 타입 / 알림 타입에 따라 어떤 작업을 할지 지정
    sender varchar(100), -- 발신자
    -- sendername varchar(50), -- 발신자 이름
    senderrefri varchar(100), -- 발신 냉장고 UUID : 일부 냉장고 관련 알림은 발신자가 냉장고일 필요가 있음
    -- senderrefriname varchar(50), -- 발신 냉장고 이름
    senderfood varchar(100), -- 음식 UUID
    receiver varchar(100), -- 수신
    -- receivername varchar(50), -- 수신자 이름
    recipeposting varchar(100), -- 레시피 포스팅 
    memo varchar(300), -- 아무거나 적을거
    alertcheck BOOLEAN NOT NULL DEFAULT FALSE, -- 알림 확인여부
    alertday TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP -- 알림 발생 시간
);

-- 테이블 확인
select * from alert;

-- 테이블 삭제
delete from alert;
drop table alert;

-- --------------------------------------자동삭제 이벤트 코드--------------------------------------

-- 이벤트 생성
SET GLOBAL event_scheduler = ON; -- DB 이벤트 스케줄러 켜기 -> NCP 콘솔에서 수행함
SHOW VARIABLES LIKE 'event_scheduler'; -- 이벤트 스케줄러 켜짐 확인

-- 1일마다 확인해서 21일(3주) 지난 알림 삭제
CREATE EVENT IF NOT EXISTS delete_old_notifications
ON SCHEDULE EVERY 1 DAY
DO
    DELETE FROM alert WHERE alertday < NOW() - INTERVAL 21 DAY;

-- 이벤트 내용 출력
SHOW EVENTS;
-- 이벤트 삭제
DROP EVENT IF EXISTS delete_old_notifications; 

-- --------------------------------------자동삭제 이벤트 테스트 코드--------------------------------------

-- 이벤트 테스트 코드
-- 1분마다 1분전 데이터 삭제하기
CREATE EVENT IF NOT EXISTS delete_old_notifications_test
ON SCHEDULE EVERY 1 MINUTE
DO
    DELETE FROM alert WHERE alertday < NOW() - INTERVAL 1 MINUTE;

-- 테스트 이벤트 삭제
DROP EVENT IF EXISTS delete_old_notifications_test;