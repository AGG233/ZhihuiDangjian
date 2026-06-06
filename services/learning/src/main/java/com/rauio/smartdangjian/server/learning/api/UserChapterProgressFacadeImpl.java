package com.rauio.smartdangjian.server.learning.api;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rauio.smartdangjian.server.learning.pojo.dto.ChapterProgressSummaryDto;
import com.rauio.smartdangjian.server.learning.service.UserChapterProgressService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserChapterProgressFacadeImpl implements UserChapterProgressFacade {

    private final UserChapterProgressService userChapterProgressService;

    @Override
    public List<ChapterProgressSummaryDto> listProgressSummariesByUserId(Long userId) {
        return userChapterProgressService.listProgressSummariesByUserId(userId);
    }

    @Override
    public long countCompletedByUserId(Long userId) {
        return userChapterProgressService.countCompletedByUserId(userId);
    }

    @Override
    public List<ChapterProgressSummaryDto> listChapterProgressSummariesByUserIds(List<Long> userIds) {
        return userChapterProgressService.listChapterProgressSummariesByUserIds(userIds);
    }
}
