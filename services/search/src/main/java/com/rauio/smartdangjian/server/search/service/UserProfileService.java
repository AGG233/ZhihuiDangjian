package com.rauio.smartdangjian.server.search.service;

import static com.rauio.smartdangjian.constants.RedisConstants.USER_PROFILE_CACHE_PREFIX;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;
import com.rauio.smartdangjian.server.content.service.course.CourseService;
import com.rauio.smartdangjian.server.learning.pojo.dto.ChapterProgressSummaryDto;
import com.rauio.smartdangjian.server.learning.pojo.dto.LearningRecordSummaryDto;
import com.rauio.smartdangjian.server.learning.service.UserChapterProgressService;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;
import com.rauio.smartdangjian.server.quiz.pojo.dto.UserQuizAnswerSummaryDto;
import com.rauio.smartdangjian.server.quiz.service.QuizService;
import com.rauio.smartdangjian.server.quiz.service.UserQuizAnswerService;
import com.rauio.smartdangjian.server.search.pojo.response.UserProfileResponse;
import com.rauio.smartdangjian.server.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private static final int QUIZ_ANSWER_CORRECT = 1;

    private final UserLearningRecordService learningRecordService;
    private final UserChapterProgressService chapterProgressService;
    private final UserQuizAnswerService quizAnswerService;
    private final QuizService quizService;
    private final ChapterService chapterService;
    private final CourseService courseService;
    private final UserService userService;

    @Cacheable(value = USER_PROFILE_CACHE_PREFIX, key = "#userId", sync = true)
    public UserProfileResponse getProfile(String userId) {
        return UserProfileResponse.builder()
                .userId(userId)
                .learning(buildLearningStats(userId))
                .knowledge(buildKnowledgeStats(userId))
                .interestCategoryIds(buildInterestCategoryIds(userId))
                .quiz(buildQuizStats(userId))
                .build();
    }

    public UserProfileResponse getCurrentUserProfile() {
        String userId = userService.getCurrentUserId();
        return getProfile(userId);
    }

    private UserProfileResponse.LearningStats buildLearningStats(String userId) {
        Long numericUserId = Long.valueOf(userId);
        List<LearningRecordSummaryDto> records = learningRecordService.listRecordSummariesByUserId(numericUserId);

        int totalDuration = records.stream()
                .mapToInt(r -> r.duration() != null ? r.duration() : 0)
                .sum();

        double avgDuration = records.isEmpty() ? 0 : (double) totalDuration / records.size();

        // 统计已完成章节数
        long completedCount = chapterProgressService.countCompletedByUserId(numericUserId);

        return UserProfileResponse.LearningStats.builder()
                .totalDuration(totalDuration)
                .avgDuration(avgDuration)
                .totalRecords(records.size())
                .completedChapters(Math.toIntExact(completedCount))
                .build();
    }

    private UserProfileResponse.KnowledgeStats buildKnowledgeStats(String userId) {
        List<ChapterProgressSummaryDto> progresses =
                chapterProgressService.listProgressSummariesByUserId(Long.valueOf(userId));

        double avgProgress = progresses.isEmpty()
                ? 0
                : progresses.stream()
                        .mapToInt(p -> p.progress() != null ? p.progress() : 0)
                        .average()
                        .orElse(0);

        long completedCount =
                progresses.stream().filter(p -> "completed".equals(p.status())).count();
        double completionRate = progresses.isEmpty() ? 0 : (double) completedCount / progresses.size();

        return UserProfileResponse.KnowledgeStats.builder()
                .avgProgress(avgProgress)
                .completionRate(completionRate)
                .build();
    }

    private List<Long> buildInterestCategoryIds(String userId) {
        // 获取用户学过的章节对应的课程分类
        List<LearningRecordSummaryDto> records =
                learningRecordService.listRecordSummariesByUserId(Long.valueOf(userId));

        if (records.isEmpty()) return Collections.emptyList();

        Set<Long> chapterIds = records.stream()
                .map(LearningRecordSummaryDto::chapterId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (chapterIds.isEmpty()) return Collections.emptyList();

        List<Long> courseIds = chapterService.listCourseIdsByChapterIds(chapterIds);

        if (courseIds.isEmpty()) return Collections.emptyList();

        return courseService.listTopCategoryIdsByCourseIds(courseIds, 5);
    }

    private UserProfileResponse.QuizStats buildQuizStats(String userId) {
        List<UserQuizAnswerSummaryDto> answers = quizAnswerService.listAnswerSummariesByUserId(Long.valueOf(userId));

        int totalAnswers = answers.size();
        int correctCount = (int) answers.stream()
                .filter(a -> Integer.valueOf(QUIZ_ANSWER_CORRECT).equals(a.isCorrect()))
                .count();
        double correctRate = totalAnswers == 0 ? 0 : (double) correctCount / totalAnswers;
        double avgTimeSpent = answers.stream()
                .filter(a -> a.timeSpent() != null)
                .mapToInt(UserQuizAnswerSummaryDto::timeSpent)
                .average()
                .orElse(0);

        // 按难度分组统计正确率
        Map<String, Double> byDifficulty = new HashMap<>();
        if (!answers.isEmpty()) {
            Set<Long> quizIds = answers.stream()
                    .map(UserQuizAnswerSummaryDto::quizId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!quizIds.isEmpty()) {
                Map<Long, String> quizDifficultyMap = quizService.getDifficultyMapByIds(quizIds);

                Map<String, List<UserQuizAnswerSummaryDto>> byDiff = answers.stream()
                        .filter(a -> quizDifficultyMap.containsKey(a.quizId()))
                        .collect(Collectors.groupingBy(a -> quizDifficultyMap.get(a.quizId())));

                for (Map.Entry<String, List<UserQuizAnswerSummaryDto>> entry : byDiff.entrySet()) {
                    List<UserQuizAnswerSummaryDto> group = entry.getValue();
                    long correct = group.stream()
                            .filter(a -> Integer.valueOf(QUIZ_ANSWER_CORRECT).equals(a.isCorrect()))
                            .count();
                    byDifficulty.put(entry.getKey(), group.isEmpty() ? 0 : (double) correct / group.size());
                }
            }
        }

        return UserProfileResponse.QuizStats.builder()
                .totalAnswers(totalAnswers)
                .correctCount(correctCount)
                .correctRate(correctRate)
                .avgTimeSpent(avgTimeSpent)
                .byDifficulty(byDifficulty)
                .build();
    }

    @CacheEvict(value = USER_PROFILE_CACHE_PREFIX, key = "#userId")
    public void evictProfile(String userId) {
        // Evicts cached user profile when underlying data changes
    }
}
