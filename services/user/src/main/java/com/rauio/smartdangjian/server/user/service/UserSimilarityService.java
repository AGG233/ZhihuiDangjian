package com.rauio.smartdangjian.server.user.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.server.user.mapper.UserSimilarityMapper;
import com.rauio.smartdangjian.server.user.pojo.dto.UserSimilaritySummaryDto;
import com.rauio.smartdangjian.server.user.pojo.entity.UserSimilarity;

@Service
public class UserSimilarityService extends ServiceImpl<UserSimilarityMapper, UserSimilarity> {

    public List<UserSimilaritySummaryDto> listTopSimilarUsers(Long userId, int limit) {
        Page<UserSimilarity> page = this.page(
                new Page<>(1, limit),
                new LambdaQueryWrapper<UserSimilarity>()
                        .eq(UserSimilarity::getUserId1, userId)
                        .orderByDesc(UserSimilarity::getSimilarityScore));
        return page.getRecords().stream()
                .map(similarity -> new UserSimilaritySummaryDto(
                        similarity.getUserId1(), similarity.getUserId2(), similarity.getSimilarityScore()))
                .toList();
    }

    public void deleteAllSimilarities() {
        this.remove(new LambdaQueryWrapper<>());
    }

    public boolean saveSummaries(List<UserSimilaritySummaryDto> summaries) {
        if (summaries == null || summaries.isEmpty()) {
            return false;
        }
        return this.saveBatch(summaries.stream()
                .map(summary -> UserSimilarity.builder()
                        .userId1(summary.userId1())
                        .userId2(summary.userId2())
                        .similarityScore(summary.similarityScore())
                        .isValid(true)
                        .build())
                .toList());
    }
}
