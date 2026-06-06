package com.rauio.smartdangjian.server.search.api;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.search.pojo.response.UserProfileResponse;

/**
 * 搜索查询外观接口 —— 为调用方提供统一的数据查询入口，
 * 屏蔽底层服务组合与数据聚合的复杂性。
 */
public interface SearchQueryFacade {

    /**
     * 获取推荐内容 ID 分页列表。
     *
     * @param userId   用户 ID
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 推荐课程 ID 的分页结果
     */
    Page<Long> recommend(Long userId, int pageNum, int pageSize);

    /**
     * 获取用户画像信息。
     *
     * @param userId 用户 ID
     * @return 用户画像视图对象
     */
    UserProfileResponse getProfile(String userId);
}
