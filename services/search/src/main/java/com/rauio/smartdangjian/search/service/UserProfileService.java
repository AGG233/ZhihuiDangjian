package com.rauio.smartdangjian.search.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.search.pojo.vo.UserProfileVO;
import com.rauio.smartdangjian.server.content.mapper.CategoryCourseMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryCourse;
import com.rauio.smartdangjian.server.learning.mapper.UserChapterProgressMapper;
import com.rauio.smartdangjian.server.learning.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserChapterProgress;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.quiz.mapper.QuizMapper;
import com.rauio.smartdangjian.server.quiz.mapper.UserQuizAnswerMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;
import com.rauio.smartdangjian.server.content.mapper.ChapterMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static com.rauio.smartdangjian.constants.RedisConstants.USER_PROFILE_CACHE_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private static final int QUIZ_ANSWER_CORRECT = 1;

    private final UserLearningRecordMapper learningRecordMapper;
    private final UserChapterProgressMapper chapterProgressMapper;
    private final UserQuizAnswerMapper quizAnswerMapper;
    private final QuizMapper quizMapper;
    private final ChapterMapper chapterMapper;
    private final CategoryCourseMapper categoryCourseMapper;
    private final UserService userService;

    @Cacheable(value = USER_PROFILE_CACHE_PREFIX, key = "#userId")
    public UserProfileVO getProfile(String userId) {
        return UserProfileVO.builder()
                .userId(userId)
                .learning(buildLearningStats(userId))
                .knowledge(buildKnowledgeStats(userId))
                .interestCategoryIds(buildInterestCategoryIds(userId))
                .quiz(buildQuizStats(userId))
                .build();
    }

    public UserProfileVO getCurrentUserProfile() {
        String userId = userService.getCurrentUserId();
        return getProfile(userId);
    }

    private UserProfileVO.LearningStats buildLearningStats(String userId) {
        List<UserLearningRecord> records = learningRecordMapper.selectList(
                new LambdaQueryWrapper<UserLearningRecord>()
                        .eq(UserLearningRecord::getUserId, userId)
        );

        int totalDuration = records.stream()
                .mapToInt(r -> r.getDuration() != null ? r.getDuration() : 0)
                .sum();

        double avgDuration = records.isEmpty() ? 0 : (double) totalDuration / records.size();

        // 统计常用设备
        String preferredDevice = records.stream()
                .filter(r -> r.getDeviceType() != null)
                .collect(Collectors.groupingBy(UserLearningRecord::getDeviceType, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // 统计已完成章节数
        Long completedCount = chapterProgressMapper.selectCount(
                new LambdaQueryWrapper<UserChapterProgress>()
                        .eq(UserChapterProgress::getUserId, userId)
                        .eq(UserChapterProgress::getStatus, "completed")
        );

        return UserProfileVO.LearningStats.builder()
                .totalDuration(totalDuration)
                .avgDuration(avgDuration)
                .totalRecords(records.size())
                .completedChapters(completedCount.intValue())
                .preferredDevice(preferredDevice)
                .build();
    }

    private UserProfileVO.KnowledgeStats buildKnowledgeStats(String userId) {
        List<UserChapterProgress> progresses = chapterProgressMapper.selectList(
                new LambdaQueryWrapper<UserChapterProgress>()
                        .eq(UserChapterProgress::getUserId, userId)
        );

        double avgProgress = progresses.isEmpty() ? 0 :
                progresses.stream().mapToInt(p -> p.getProgress() != null ? p.getProgress() : 0)
                        .average().orElse(0);

        long completedCount = progresses.stream()
                .filter(p -> "completed".equals(p.getStatus()))
                .count();
        double completionRate = progresses.isEmpty() ? 0 : (double) completedCount / progresses.size();

        List<String> weakChapterIds = progresses.stream()
                .filter(p -> p.getProgress() != null && p.getProgress() < 50)
                .map(UserChapterProgress::getChapterId)
                .toList();

        return UserProfileVO.KnowledgeStats.builder()
                .avgProgress(avgProgress)
                .completionRate(completionRate)
                .weakChapterIds(weakChapterIds)
                .build();
    }

    private List<String> buildInterestCategoryIds(String userId) {
        // 获取用户学过的章节对应的课程分类
        List<UserLearningRecord> records = learningRecordMapper.selectList(
                new LambdaQueryWrapper<UserLearningRecord>()
                        .eq(UserLearningRecord::getUserId, userId)
                        .select(UserLearningRecord::getChapterId)
        );

        if (records.isEmpty()) return Collections.emptyList();

        Set<String> chapterIds = records.stream()
                .map(UserLearningRecord::getChapterId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (chapterIds.isEmpty()) return Collections.emptyList();

        List<Chapter> chapters = chapterMapper.selectList(
                new LambdaQueryWrapper<Chapter>()
                        .in(Chapter::getId, chapterIds)
                        .select(Chapter::getCourseId)
        );

        Set<String> courseIds = chapters.stream()
                .map(Chapter::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (courseIds.isEmpty()) return Collections.emptyList();

        List<CategoryCourse> relations = categoryCourseMapper.selectList(
                new LambdaQueryWrapper<CategoryCourse>()
                        .in(CategoryCourse::getCourseId, courseIds)
        );

        // 按分类出现频次排序
        return relations.stream()
                .map(CategoryCourse::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(categoryId -> categoryId, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(5)
                .toList();
    }

    private UserProfileVO.QuizStats buildQuizStats(String userId) {
        List<UserQuizAnswer> answers = quizAnswerMapper.selectList(
                new LambdaQueryWrapper<UserQuizAnswer>()
                        .eq(UserQuizAnswer::getUserId, userId)
        );

        int totalAnswers = answers.size();
        int correctCount = (int) answers.stream()
                .filter(a -> Integer.valueOf(QUIZ_ANSWER_CORRECT).equals(a.getIsCorrect()))
                .count();
        double correctRate = totalAnswers == 0 ? 0 : (double) correctCount / totalAnswers;
        double avgTimeSpent = answers.stream()
                .filter(a -> a.getTimeSpent() != null)
                .mapToInt(UserQuizAnswer::getTimeSpent)
                .average().orElse(0);

        // 按难度分组统计正确率
        Map<String, Double> byDifficulty = new HashMap<>();
        if (!answers.isEmpty()) {
            Set<String> quizIds = answers.stream()
                    .map(UserQuizAnswer::getQuizId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!quizIds.isEmpty()) {
                List<Quiz> quizzes = quizMapper.selectList(
                        new LambdaQueryWrapper<Quiz>()
                                .in(Quiz::getId, quizIds)
                                .select(Quiz::getId, Quiz::getDifficulty)
                );
                Map<String, String> quizDifficultyMap = quizzes.stream()
                        .filter(q -> q.getDifficulty() != null)
                        .collect(Collectors.toMap(Quiz::getId, Quiz::getDifficulty));

                Map<String, List<UserQuizAnswer>> byDiff = answers.stream()
                        .filter(a -> quizDifficultyMap.containsKey(a.getQuizId()))
                        .collect(Collectors.groupingBy(a -> quizDifficultyMap.get(a.getQuizId())));

                for (Map.Entry<String, List<UserQuizAnswer>> entry : byDiff.entrySet()) {
                    List<UserQuizAnswer> group = entry.getValue();
                    long correct = group.stream()
                            .filter(a -> Integer.valueOf(QUIZ_ANSWER_CORRECT).equals(a.getIsCorrect()))
                            .count();
                    byDifficulty.put(entry.getKey(), group.isEmpty() ? 0 : (double) correct / group.size());
                }
            }
        }

        return UserProfileVO.QuizStats.builder()
                .totalAnswers(totalAnswers)
                .correctCount(correctCount)
                .correctRate(correctRate)
                .avgTimeSpent(avgTimeSpent)
                .byDifficulty(byDifficulty)
                .build();
    }
}
