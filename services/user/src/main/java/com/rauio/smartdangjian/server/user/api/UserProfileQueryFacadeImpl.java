package com.rauio.smartdangjian.server.user.api;

import org.springframework.stereotype.Service;

import com.rauio.smartdangjian.server.user.pojo.response.UserResponse;
import com.rauio.smartdangjian.server.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserProfileQueryFacadeImpl implements UserProfileQueryFacade {

    private final UserService userService;

    @Override
    public String getCurrentUserId() {
        return userService.getCurrentUserId();
    }

    @Override
    public UserResponse getUserById(String userId) {
        if (userId == null || userId.isEmpty()) {
            return null;
        }
        return userService.get(Long.valueOf(userId));
    }
}
