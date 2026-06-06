package com.rauio.smartdangjian.server.user.api;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rauio.smartdangjian.server.user.pojo.dto.UserSimilaritySummaryDto;
import com.rauio.smartdangjian.server.user.service.UserSimilarityService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserSimilarityQueryFacadeImpl implements UserSimilarityQueryFacade {

    private final UserSimilarityService userSimilarityService;

    @Override
    public List<UserSimilaritySummaryDto> listTopSimilarUsers(Long userId, int limit) {
        return userSimilarityService.listTopSimilarUsers(userId, limit);
    }

    @Override
    public void deleteAllSimilarities() {
        userSimilarityService.deleteAllSimilarities();
    }

    @Override
    public boolean saveSummaries(List<UserSimilaritySummaryDto> summaries) {
        return userSimilarityService.saveSummaries(summaries);
    }
}
