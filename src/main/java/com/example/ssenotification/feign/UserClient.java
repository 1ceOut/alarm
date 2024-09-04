package com.example.ssenotification.feign;

import com.example.ssenotification.data.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "userClient", url = "https://api.icebuckwheat.kro.kr")
public interface UserClient {
    @GetMapping("/api/login/getalluser")
    List<UserDto> getAllUsers();
}
