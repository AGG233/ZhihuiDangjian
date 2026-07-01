package com.rauio.smartdangjian.server.search.api;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.course.pojo.response.CourseResponse;
import com.rauio.smartdangjian.server.search.pojo.response.UserProfileResponse;
import com.rauio.smartdangjian.server.search.service.RecommendService;
import com.rauio.smartdangjian.server.search.service.UserProfileService;

import lombok.RequiredArgsConstructor;

/**
 * SearchQueryFacade 默认实现。
 *
 * <p>将推荐与画像查询请求委托给对应的领域服务。
 */
@Component
@RequiredArgsConstructor
public class SearchQueryFacadeImpl implements SearchQueryFacade {

    private final RecommendService recommendService;
    private final UserProfileService userProfileService;

    @Override
    public Page<CourseResponse> recommend(Long userId, int pageNum, int pageSize) {
        return recommendService.recommend(userId, pageNum, pageSize);
    }

    @Override
    public UserProfileResponse getProfile(String userId) {
        return userProfileService.getProfile(userId);
    }
}
