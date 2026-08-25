package com.rauio.smartdangjian.server.auth.security;

import java.util.List;

import org.springframework.stereotype.Component;

import com.rauio.smartdangjian.server.auth.constants.JwtClaims;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;

/**
 * RBAC 角色提供器（无状态 JWT 版）。
 *
 * <p>Stateless 模式下服务端不保存会话，角色从访问令牌的 {@code role} claim
 * 读取（登录时由 AuthService 写入），并按 MANAGER &gt; SCHOOL &gt; STUDENT
 * 层级展开为完整角色列表。
 */
@Component
public class SaTokenPermissionImpl implements StpInterface {

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Object role = StpUtil.getExtra(JwtClaims.ROLE);
        if (!(role instanceof String roleName)) {
            return List.of();
        }
        return switch (roleName) {
            case "MANAGER" -> List.of("STUDENT", "SCHOOL", "MANAGER");
            case "SCHOOL" -> List.of("STUDENT", "SCHOOL");
            case "STUDENT" -> List.of("STUDENT");
            default -> List.of();
        };
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }
}
