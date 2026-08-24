package com.rauio.smartdangjian.server.auth.service;

import org.springframework.stereotype.Service;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.auth.constants.AuthErrorConstants;
import com.rauio.smartdangjian.server.auth.constants.JwtClaims;

import cn.dev33.satoken.stp.StpUtil;
import lombok.RequiredArgsConstructor;

/**
 * 无状态访问令牌守卫：校验令牌内版本号与 Redis 当前版本号一致。
 *
 * <p>作为 {@code TokenVersionService#bump} 的消费端，实现「改密/封禁即全端下线」：
 * bump 后存量令牌中的 {@code ver} claim 小于 Redis 当前值，校验即拒绝。
 * 仅在生产链路启用（dev/test 由拦截器侧跳过，避免依赖外部 Redis）。
 */
@Service
@RequiredArgsConstructor
public class StatelessTokenGuard {

    private final TokenVersionService tokenVersionService;

    /**
     * 校验当前请求持有的访问令牌版本号是否仍然有效。
     *
     * <p>令牌未携带 {@code ver} claim（如历史令牌或开发自动登录）按 0 处理，
     * 只要 Redis 中同样未被 bump 过即放行，保持向后兼容。
     *
     * @throws BusinessException 版本号落后于服务端当前值（令牌已被强制下线）
     */
    public void verify() {
        Long loginId = StpUtil.getLoginIdDefaultNull() == null ? null : Long.valueOf(StpUtil.getLoginIdAsString());
        if (loginId == null) {
            return;
        }
        long claimed = 0L;
        Object claim = StpUtil.getExtra(JwtClaims.TOKEN_VERSION);
        if (claim instanceof Number n) {
            claimed = n.longValue();
        } else if (claim != null) {
            try {
                claimed = Long.parseLong(String.valueOf(claim));
            } catch (NumberFormatException ignored) {
                claimed = 0L;
            }
        }
        if (claimed < tokenVersionService.current(loginId)) {
            throw new BusinessException(AuthErrorConstants.UNAUTHORIZED, "登录状态已失效，请重新登录");
        }
    }
}
