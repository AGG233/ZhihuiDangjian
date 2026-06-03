package com.rauio.smartdangjian.server.learning.api;

import java.util.List;

import com.rauio.smartdangjian.server.learning.pojo.dto.ChapterProgressSummaryDto;

/**
 * 章节进度查询门面 —— 供搜索模块等业务方调用的稳定接口。
 */
public interface UserChapterProgressFacade {

    List<ChapterProgressSummaryDto> listProgressSummariesByUserId(Long userId);

    long countCompletedByUserId(Long userId);

    List<ChapterProgressSummaryDto> listChapterProgressSummariesByUserIds(List<Long> userIds);
}
