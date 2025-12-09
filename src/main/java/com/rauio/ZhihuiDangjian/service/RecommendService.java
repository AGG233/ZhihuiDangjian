package com.rauio.ZhihuiDangjian.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.ZhihuiDangjian.dao.ChapterDao;
import com.rauio.ZhihuiDangjian.mapper.UserChapterProgressMapper;
import com.rauio.ZhihuiDangjian.mapper.UserLearningRecordMapper;
import com.rauio.ZhihuiDangjian.mapper.UserSimilarityMapper;
import com.rauio.ZhihuiDangjian.pojo.Chapter;
import com.rauio.ZhihuiDangjian.pojo.UserChapterProgress;
import com.rauio.ZhihuiDangjian.pojo.UserLearningRecord;
import com.rauio.ZhihuiDangjian.pojo.UserSimilarity;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendService {

    private final UserLearningRecordMapper userLearningRecordMapper;
    private final UserChapterProgressMapper userChapterProgressMapper;
    private final UserSimilarityMapper userSimilarityMapper;
    private final ChapterDao chapterDao;

    private static double calculateCosineSimilarity(Set<Long> items1, Set<Long> items2) {

        if (items1.isEmpty() || items2.isEmpty()) {
            return 0.0;
        }

        Set<Long> intersection = new HashSet<>(items1);
        intersection.retainAll(items2);
        int commonCount = intersection.size();

        if (commonCount == 0) {
            return 0.0;
        }
        double denominator = Math.sqrt(items1.size() * items2.size());
        return (double) Math.round((commonCount / denominator) * 1000) / 1000;
    }

    public Page<Long> getRecommendByCF(String userId, int pageNum, int pageSize) {
        LambdaQueryWrapper<UserSimilarity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserSimilarity::getUserId1, userId).orderByDesc(UserSimilarity::getSimilarityScore);

        // 创建分页对象
        Page<UserSimilarity> page = new Page<>(pageNum, pageSize);

        // 执行分页查询
        userSimilarityMapper.selectPage(page, queryWrapper);

        // 提取推荐的用户ID列表（相似用户）
        List<Long> similarUserIds = page.getRecords().stream()
                .map(UserSimilarity::getUserId2)
                .collect(Collectors.toList());

        // 根据相似用户ID查找他们学习过的章节
        Set<Long> chapterIds = new HashSet<>();
        if (!similarUserIds.isEmpty()) {
            // 查询相似用户的学习记录
            LambdaQueryWrapper<UserLearningRecord> learningRecordQuery = new LambdaQueryWrapper<>();
            learningRecordQuery.in(UserLearningRecord::getUserId, similarUserIds)
                    .select(UserLearningRecord::getChapterId);
            List<UserLearningRecord> learningRecords = userLearningRecordMapper.selectList(learningRecordQuery);

            // 查询相似用户的章节进度
            LambdaQueryWrapper<UserChapterProgress> chapterProgressQuery = new LambdaQueryWrapper<>();
            chapterProgressQuery.in(UserChapterProgress::getUserId, similarUserIds)
                    .select(UserChapterProgress::getChapterId);
            List<UserChapterProgress> chapterProgresses = userChapterProgressMapper.selectList(chapterProgressQuery);

            // 合并章节ID
            chapterIds.addAll(learningRecords.stream()
                    .map(UserLearningRecord::getChapterId)
                    .collect(Collectors.toSet()));
            chapterIds.addAll(chapterProgresses.stream()
                    .map(UserChapterProgress::getChapterId)
                    .collect(Collectors.toSet()));
        }

        // 根据章节ID获取对应的课程ID
        Set<Long> courseIds = new HashSet<>();
        if (!chapterIds.isEmpty()) {
            for (Long chapterId : chapterIds) {
                Chapter chapter = chapterDao.getById(chapterId);
                if (chapter != null) {
                    courseIds.add(chapter.getCourseId());
                }
            }
        }
        // 构造返回结果
        Page<Long> resultPage = new Page<>(pageNum, pageSize);
        resultPage.setTotal(courseIds.size());

        // 分页处理
        List<Long> courseIdsList = new ArrayList<>(courseIds);
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, courseIdsList.size());

        if (fromIndex < courseIdsList.size()) {
            resultPage.setRecords(courseIdsList.subList(fromIndex, toIndex));
        } else {
            resultPage.setRecords(new ArrayList<>());
        }

        return resultPage;
    }

    @Scheduled(cron = "0 0 * * * ?")
    private void calculateSimilarity(){

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(now, LocalDateTime.now());

        List<UserSimilarity> list = new ArrayList<>();

        LambdaQueryWrapper<UserLearningRecord> queryWrapper1 = new LambdaQueryWrapper<>();
        LambdaQueryWrapper<UserChapterProgress> queryWrapper2 = new LambdaQueryWrapper<>();

        //构造查询条件
        queryWrapper1.select(UserLearningRecord::getUserId, UserLearningRecord::getChapterId);
        queryWrapper2.select(UserChapterProgress::getUserId, UserChapterProgress::getChapterId);

        // 查询相关行为
        List<UserLearningRecord>    learningRecords     = userLearningRecordMapper
                .selectList(queryWrapper1);
        List<UserChapterProgress>   chapterProgresses   = userChapterProgressMapper
                .selectList(queryWrapper2);


        //将用户行为数据分组，以用户ID为键，行为数据为值
        Map<Long, Set<Long>> learningRecordsMap = learningRecords.stream()
                .collect(Collectors.groupingBy(
                        UserLearningRecord::getUserId,
                        Collectors.mapping(
                                UserLearningRecord::getChapterId,
                                Collectors.toSet()
                        )
                ));
        Map<Long, Set<Long>> chapterProgressMap = chapterProgresses.stream()
                .collect(Collectors.groupingBy(
                        UserChapterProgress::getUserId,
                        Collectors.mapping(
                                UserChapterProgress::getChapterId,
                                Collectors.toSet()
                        )
                ));
        //合并数据
        learningRecordsMap.forEach((key, value) -> {
            chapterProgressMap.merge(key, value, (set1, set2) -> {
                set1.addAll(set2);
                return set1;
            });
        });


        for (Long userA : learningRecordsMap.keySet()) {
            for (Long userB : learningRecordsMap.keySet()) {
                Set<Long> similarUserA = learningRecordsMap.get(userA);
                Set<Long> similarUserB = learningRecordsMap.get(userB);

                double score = calculateCosineSimilarity(similarUserA, similarUserB);

                if (score > 0.1) {
                    // 存 A -> B
                    list.add(UserSimilarity.builder().userId1(userA).userId2(userB).similarityScore(score).build());
                    // 存 B -> A (为了查询方便，通常存双向)
                    list.add(UserSimilarity.builder().userId1(userB).userId2(userA).similarityScore(score).build());
                }
            }
        }
        userSimilarityMapper.delete(null);
        userSimilarityMapper.insert(list);
    }
}