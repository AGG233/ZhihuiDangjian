package com.rauio.smartdangjian.server.auth.security;

import java.util.List;

import org.springframework.stereotype.Component;

import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.utils.spec.UserType;

import cn.dev33.satoken.stp.StpInterface;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SaTokenPermissionImpl implements StpInterface {

    private final UserMapper userMapper;

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        User user = userMapper.selectById(loginId.toString());
        if (user == null || user.getUserType() == null) {
            return List.of();
        }
        return switch (user.getUserType()) {
            case MANAGER -> List.of("STUDENT", "SCHOOL", "MANAGER");
            case SCHOOL -> List.of("STUDENT", "SCHOOL");
            case STUDENT -> List.of("STUDENT");
        };
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }
}
