package com.example.ssenotification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class SseNotificationApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(SseNotificationApplication.class, args);
    }
}
