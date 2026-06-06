package com.rauio.smartdangjian.server.user.api;

import java.util.List;

import com.rauio.smartdangjian.server.user.pojo.dto.UserSimilaritySummaryDto;

/**
 * 用户相似度查询门面 —— 供搜索模块等业务方调用的稳定接口。
 */
public interface UserSimilarityQueryFacade {

    List<UserSimilaritySummaryDto> listTopSimilarUsers(Long userId, int limit);

    void deleteAllSimilarities();

    boolean saveSummaries(List<UserSimilaritySummaryDto> summaries);
}
