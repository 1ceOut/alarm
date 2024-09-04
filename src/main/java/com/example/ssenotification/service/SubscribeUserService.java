package com.example.ssenotification.service;

import com.example.ssenotification.data.UserDto;
import com.example.ssenotification.feign.MultipleUserDataFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscribeUserService {
    private final MultipleUserDataFeign multipleUserDataFeign;

    @Autowired
    public SubscribeUserService(MultipleUserDataFeign multipleUserDataFeign) {
        this.multipleUserDataFeign = multipleUserDataFeign;
    }

    // 어떤 사람의 ID를 통해 해당 사람을 구독한 사람들의 데이터 받아옴
    public List<String> getUserIdsBySubScribeUser(String userId) {
        // Feign 클라이언트를 사용하여 실제 사용자 데이터를 가져옴
        List<UserDto> users = multipleUserDataFeign.getSubscribeUser(userId);

        // UserDto 리스트에서 userId만 추출하여 반환
        return users.stream()
                .map(UserDto::getUserId)
                .collect(Collectors.toList());
    }
}
