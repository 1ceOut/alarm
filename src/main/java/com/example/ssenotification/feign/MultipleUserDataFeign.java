package com.example.ssenotification.feign;

import com.example.ssenotification.data.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name ="UserListApi", url="https://api.icebuckwheat.kro.kr/api")
public interface MultipleUserDataFeign {
    @GetMapping("/food/find/refriUser")
    List<UserDto> getRefrigeratorUser(@RequestParam("refrigerator_id") String refrigeratorId);

    @GetMapping("/food/find/Usersub")
    List<UserDto> getSubscribeUser(@RequestParam("userId") String userId);

    @GetMapping("/food/find/refriName")
    String getRefriName(@RequestParam("refrigerator_id") String refrigeratorId);
}
