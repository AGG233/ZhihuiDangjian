package com.rauio.smartdangjian.server.auth.security;

import java.util.List;

import org.springframework.stereotype.Component;

import com.rauio.smartdangjian.server.user.pojo.entity.User;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;

@Component
public class SaTokenPermissionImpl implements StpInterface {

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Object userObj = StpUtil.getSession().get("user");
        if (userObj instanceof User user && user.getUserType() != null) {
            return switch (user.getUserType()) {
                case MANAGER -> List.of("STUDENT", "SCHOOL", "MANAGER");
                case SCHOOL -> List.of("STUDENT", "SCHOOL");
                case STUDENT -> List.of("STUDENT");
            };
        }
        return List.of();
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }
}
