package com.example.ssenotification.service;

import com.example.ssenotification.data.UserDto;
import com.example.ssenotification.feign.MultipleUserDataFeign;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RefrigeratorUserService {

    private final MultipleUserDataFeign multipleUserDataFeign;

    @Autowired
    public RefrigeratorUserService(MultipleUserDataFeign multipleUserDataFeign) {
        this.multipleUserDataFeign = multipleUserDataFeign;
    }

    // 특정 냉장고 ID에 속한 모든 사용자 ID를 반환하는 메서드
    public List<String> getUserIdsByRefrigeratorId(String refrigeratorId) {
        // Feign 클라이언트를 사용하여 실제 사용자 데이터를 가져옴
        List<UserDto> users = multipleUserDataFeign.getRefrigeratorUser(refrigeratorId);

        // UserDto 리스트에서 userId만 추출하여 반환
        return users.stream()
                .map(UserDto::getUserId)
                .collect(Collectors.toList());
    }

    public String getRefrigeratorName(String refrigeratorId) {
        //String refrigeratorName = multipleUserDataFeign.getRefriName(refrigeratorId);
        return multipleUserDataFeign.getRefriName(refrigeratorId);
    }


}
