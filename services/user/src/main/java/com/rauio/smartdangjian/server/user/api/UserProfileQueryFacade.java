package com.rauio.smartdangjian.server.user.api;

import com.rauio.smartdangjian.server.user.pojo.response.UserResponse;

/**
 * 用户基本信息查询门面 —— 供搜索/AI 模块等业务方调用的稳定接口。
 */
public interface UserProfileQueryFacade {

    String getCurrentUserId();

    /**
     * 根据用户 ID 获取用户完整信息。
     *
     * @param userId 用户 ID
     * @return 用户完整信息，用户不存在时返回 {@code null}
     */
    UserResponse getUserById(String userId);
}
