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
        Object userObj = StpUtil.getSession().get("user");
        if (userObj instanceof User user && user.getUserType() != null) {
            return switch (user.getUserType()) {
                case MANAGER -> List.of("*");
                case SCHOOL ->
                    List.of(
                            "category:*",
                            "chapter:*",
                            "course:*",
                            "quiz:*",
                            "resource:*",
                            "user:read",
                            "user:update",
                            "content:*",
                            "article:*",
                            "learning:read",
                            "graph:read",
                            "search:*",
                            "file:*");
                case STUDENT ->
                    List.of(
                            "content:read",
                            "article:read",
                            "course:read",
                            "chapter:read",
                            "quiz:answer",
                            "quiz:read",
                            "learning:*",
                            "file:read",
                            "category:read",
                            "search:*",
                            "graph:read",
                            "user:read");
            };
        }
        return List.of();
    }
}
