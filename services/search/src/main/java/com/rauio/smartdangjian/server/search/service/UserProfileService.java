package com.rauio.smartdangjian.server.search.service;

import static com.rauio.smartdangjian.constants.RedisConstants.USER_PROFILE_CACHE_PREFIX;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.server.content.mapper.CategoryCourseMapper;
import com.rauio.smartdangjian.server.content.mapper.ChapterMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryCourse;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.learning.mapper.UserChapterProgressMapper;
import com.rauio.smartdangjian.server.learning.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserChapterProgress;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.quiz.mapper.QuizMapper;
import com.rauio.smartdangjian.server.quiz.mapper.UserQuizAnswerMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;
import com.rauio.smartdangjian.server.search.pojo.response.DynamicProfileResponse;
import com.rauio.smartdangjian.server.search.pojo.response.LearningSummaryResponse;
import com.rauio.smartdangjian.server.search.pojo.response.UserProfileResponse;
import com.rauio.smartdangjian.server.user.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private static final int QUIZ_ANSWER_CORRECT = 1;

    private static final int HOT_TAG_DAYS = 30;
    private static final int HOT_TAG_LIMIT = 3;
    private static final int TREND_WEEKS = 8;
    private static final double WEAK_THRESHOLD = 0.5;

    private final UserLearningRecordMapper learningRecordMapper;
    private final UserChapterProgressMapper chapterProgressMapper;
    private final UserQuizAnswerMapper quizAnswerMapper;
    private final QuizMapper quizMapper;
    private final ChapterMapper chapterMapper;
    private final CategoryCourseMapper categoryCourseMapper;
    private final UserService userService;

    @Cacheable(value = USER_PROFILE_CACHE_PREFIX, key = "#userId")
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

    public DynamicProfileResponse getCurrentUserDynamicProfile() {
        String userId = userService.getCurrentUserId();
        return buildDynamicProfile(userId);
    }

    public LearningSummaryResponse getCurrentUserLearningSummary() {
        String userId = userService.getCurrentUserId();
        return getLearningSummary(userId);
    }

    public DynamicProfileResponse buildDynamicProfile(String userId) {
        return DynamicProfileResponse.builder()
                .hotTags(buildRecentHotTags(userId))
                .growthTrend(buildGrowthTrend(userId))
                .weakDomains(buildWeakDomains(userId))
                .build();
    }

    public LearningSummaryResponse getLearningSummary(String userId) {
        UserProfileResponse.LearningStats learning = buildLearningStats(userId);
        UserProfileResponse.KnowledgeStats knowledge = buildKnowledgeStats(userId);
        UserProfileResponse.QuizStats quiz = buildQuizStats(userId);
        return LearningSummaryResponse.builder()
                .theory(LearningSummaryResponse.TheoryDimension.builder()
                        .totalDuration(learning.getTotalDuration())
                        .completionRate(knowledge.getCompletionRate())
                        .build())
                .policyComprehension(LearningSummaryResponse.PolicyDimension.builder()
                        .avgCorrectRate(quiz.getCorrectRate())
                        .totalAnswers(quiz.getTotalAnswers())
                        .build())
                .build();
    }

    private UserProfileResponse.LearningStats buildLearningStats(String userId) {
        List<UserLearningRecord> records = learningRecordMapper.selectList(
                new LambdaQueryWrapper<UserLearningRecord>().eq(UserLearningRecord::getUserId, userId));

        int totalDuration = records.stream()
                .mapToInt(r -> r.getDuration() != null ? r.getDuration() : 0)
                .sum();

        double avgDuration = records.isEmpty() ? 0 : (double) totalDuration / records.size();

        // 统计常用设备
        String preferredDevice = records.stream()
                .filter(r -> r.getDeviceType() != null)
                .collect(Collectors.groupingBy(UserLearningRecord::getDeviceType, Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        // 统计已完成章节数
        Long completedCount = chapterProgressMapper.selectCount(new LambdaQueryWrapper<UserChapterProgress>()
                .eq(UserChapterProgress::getUserId, userId)
                .eq(UserChapterProgress::getStatus, "completed"));

        return UserProfileResponse.LearningStats.builder()
                .totalDuration(totalDuration)
                .avgDuration(avgDuration)
                .totalRecords(records.size())
                .completedChapters(completedCount.intValue())
                .preferredDevice(preferredDevice)
                .build();
    }

    private UserProfileResponse.KnowledgeStats buildKnowledgeStats(String userId) {
        List<UserChapterProgress> progresses = chapterProgressMapper.selectList(
                new LambdaQueryWrapper<UserChapterProgress>().eq(UserChapterProgress::getUserId, userId));

        double avgProgress = progresses.isEmpty()
                ? 0
                : progresses.stream()
                        .mapToInt(p -> p.getProgress() != null ? p.getProgress() : 0)
                        .average()
                        .orElse(0);

        long completedCount = progresses.stream()
                .filter(p -> "completed".equals(p.getStatus()))
                .count();
        double completionRate = progresses.isEmpty() ? 0 : (double) completedCount / progresses.size();

        List<Long> weakChapterIds = progresses.stream()
                .filter(p -> p.getProgress() != null && p.getProgress() < 50)
                .map(UserChapterProgress::getChapterId)
                .toList();

        return UserProfileResponse.KnowledgeStats.builder()
                .avgProgress(avgProgress)
                .completionRate(completionRate)
                .weakChapterIds(weakChapterIds)
                .build();
    }

    private List<Long> buildInterestCategoryIds(String userId) {
        // 获取用户学过的章节对应的课程分类
        List<UserLearningRecord> records = learningRecordMapper.selectList(new LambdaQueryWrapper<UserLearningRecord>()
                .eq(UserLearningRecord::getUserId, userId)
                .select(UserLearningRecord::getChapterId));

        if (records.isEmpty()) return Collections.emptyList();

        Set<Long> chapterIds = records.stream()
                .map(UserLearningRecord::getChapterId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (chapterIds.isEmpty()) return Collections.emptyList();

        List<Chapter> chapters = chapterMapper.selectList(
                new LambdaQueryWrapper<Chapter>().in(Chapter::getId, chapterIds).select(Chapter::getCourseId));

        Set<Long> courseIds = chapters.stream()
                .map(Chapter::getCourseId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (courseIds.isEmpty()) return Collections.emptyList();

        List<CategoryCourse> relations = categoryCourseMapper.selectList(
                new LambdaQueryWrapper<CategoryCourse>().in(CategoryCourse::getCourseId, courseIds));

        // 按分类出现频次排序
        return relations.stream()
                .map(CategoryCourse::getCategoryId)
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(categoryId -> categoryId, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(5)
                .toList();
    }

    private UserProfileResponse.QuizStats buildQuizStats(String userId) {
        List<UserQuizAnswer> answers = quizAnswerMapper.selectList(
                new LambdaQueryWrapper<UserQuizAnswer>().eq(UserQuizAnswer::getUserId, userId));

        int totalAnswers = answers.size();
        int correctCount = (int) answers.stream()
                .filter(a -> Integer.valueOf(QUIZ_ANSWER_CORRECT).equals(a.getIsCorrect()))
                .count();
        double correctRate = totalAnswers == 0 ? 0 : (double) correctCount / totalAnswers;
        double avgTimeSpent = answers.stream()
                .filter(a -> a.getTimeSpent() != null)
                .mapToInt(UserQuizAnswer::getTimeSpent)
                .average()
                .orElse(0);

        // 按难度分组统计正确率
        Map<String, Double> byDifficulty = new HashMap<>();
        if (!answers.isEmpty()) {
            Set<Long> quizIds = answers.stream()
                    .map(UserQuizAnswer::getQuizId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            if (!quizIds.isEmpty()) {
                List<Quiz> quizzes = quizMapper.selectList(new LambdaQueryWrapper<Quiz>()
                        .in(Quiz::getId, quizIds)
                        .select(Quiz::getId, Quiz::getDifficulty));
                Map<Long, String> quizDifficultyMap = quizzes.stream()
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

        return UserProfileResponse.QuizStats.builder()
                .totalAnswers(totalAnswers)
                .correctCount(correctCount)
                .correctRate(correctRate)
                .avgTimeSpent(avgTimeSpent)
                .byDifficulty(byDifficulty)
                .build();
    }

    private List<DynamicProfileResponse.HotTag> buildRecentHotTags(String userId) {
        LocalDateTime since = LocalDateTime.now().minusDays(HOT_TAG_DAYS);
        List<UserLearningRecord> records = learningRecordMapper.selectList(new LambdaQueryWrapper<UserLearningRecord>()
                .eq(UserLearningRecord::getUserId, userId)
                .ge(UserLearningRecord::getCreatedAt, since)
                .select(UserLearningRecord::getChapterId));

        Set<Long> chapterIds = records.stream()
                .map(UserLearningRecord::getChapterId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (chapterIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Chapter> chapters = chapterMapper.selectList(new LambdaQueryWrapper<Chapter>()
                .in(Chapter::getId, chapterIds)
                .select(Chapter::getId, Chapter::getTitle));
        Map<Long, String> titleMap = chapters.stream()
                .filter(c -> c.getTitle() != null)
                .collect(Collectors.toMap(Chapter::getId, Chapter::getTitle, (a, b) -> a));

        return records.stream()
                .map(UserLearningRecord::getChapterId)
                .filter(Objects::nonNull)
                .filter(titleMap::containsKey)
                .collect(Collectors.groupingBy(titleMap::get, Collectors.counting()))
                .entrySet()
                .stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(HOT_TAG_LIMIT)
                .map(e -> DynamicProfileResponse.HotTag.builder()
                        .tag(e.getKey())
                        .count(e.getValue())
                        .build())
                .toList();
    }

    private List<DynamicProfileResponse.GrowthTrend> buildGrowthTrend(String userId) {
        LocalDate thisWeekStart = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate firstWeekStart = thisWeekStart.minusWeeks(TREND_WEEKS - 1L);

        List<UserLearningRecord> records = learningRecordMapper.selectList(new LambdaQueryWrapper<UserLearningRecord>()
                .eq(UserLearningRecord::getUserId, userId)
                .ge(UserLearningRecord::getStartTime, firstWeekStart.atStartOfDay())
                .select(
                        UserLearningRecord::getStartTime,
                        UserLearningRecord::getCreatedAt,
                        UserLearningRecord::getDuration));

        List<UserQuizAnswer> answers = quizAnswerMapper.selectList(new LambdaQueryWrapper<UserQuizAnswer>()
                .eq(UserQuizAnswer::getUserId, userId)
                .ge(UserQuizAnswer::getAnswerTime, firstWeekStart.atStartOfDay())
                .select(UserQuizAnswer::getAnswerTime, UserQuizAnswer::getIsCorrect));

        List<DynamicProfileResponse.GrowthTrend> trend = new ArrayList<>(TREND_WEEKS);
        for (int i = 0; i < TREND_WEEKS; i++) {
            LocalDate weekStart = firstWeekStart.plusWeeks(i);

            int duration = records.stream()
                    .filter(r -> isInWeek(weekOf(r.getStartTime(), r.getCreatedAt()), weekStart))
                    .mapToInt(r -> r.getDuration() != null ? r.getDuration() : 0)
                    .sum();

            long correct = answers.stream()
                    .filter(a -> isInWeek(weekOf(a.getAnswerTime(), null), weekStart))
                    .filter(a -> Integer.valueOf(QUIZ_ANSWER_CORRECT).equals(a.getIsCorrect()))
                    .count();
            long total = answers.stream()
                    .filter(a -> isInWeek(weekOf(a.getAnswerTime(), null), weekStart))
                    .count();
            double accuracy = total == 0 ? 0 : (double) correct / total;

            trend.add(DynamicProfileResponse.GrowthTrend.builder()
                    .weekStart(weekStart)
                    .studyDuration(duration)
                    .quizAccuracy(accuracy)
                    .build());
        }
        return trend;
    }

    private LocalDate weekOf(LocalDateTime startTime, LocalDateTime createdAt) {
        LocalDateTime base = startTime != null ? startTime : createdAt;
        return base == null ? null : base.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private boolean isInWeek(LocalDate weekStart, LocalDate targetWeekStart) {
        return weekStart != null && weekStart.equals(targetWeekStart);
    }

    private List<DynamicProfileResponse.WeakDomain> buildWeakDomains(String userId) {
        List<DynamicProfileResponse.WeakDomain> weakDomains = new ArrayList<>();

        UserProfileResponse.QuizStats quizStats = buildQuizStats(userId);
        quizStats.getByDifficulty().forEach((difficulty, rate) -> {
            if (rate < WEAK_THRESHOLD) {
                weakDomains.add(DynamicProfileResponse.WeakDomain.builder()
                        .type("DIFFICULTY")
                        .name(difficulty)
                        .accuracy(rate)
                        .build());
            }
        });

        List<UserQuizAnswer> answers = quizAnswerMapper.selectList(
                new LambdaQueryWrapper<UserQuizAnswer>().eq(UserQuizAnswer::getUserId, userId));
        if (answers.isEmpty()) {
            return weakDomains;
        }

        Set<Long> quizIds = answers.stream()
                .map(UserQuizAnswer::getQuizId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        if (quizIds.isEmpty()) {
            return weakDomains;
        }

        List<Quiz> quizzes = quizMapper.selectList(
                new LambdaQueryWrapper<Quiz>().in(Quiz::getId, quizIds).select(Quiz::getId, Quiz::getChapterId));
        Map<Long, Long> quizChapterMap = quizzes.stream()
                .filter(q -> q.getChapterId() != null)
                .collect(Collectors.toMap(Quiz::getId, Quiz::getChapterId, (a, b) -> a));

        Map<Long, Long> chapterCounts = new HashMap<>();
        Map<Long, Long> chapterCorrect = new HashMap<>();
        for (UserQuizAnswer answer : answers) {
            Long chapterId = quizChapterMap.get(answer.getQuizId());
            if (chapterId == null) {
                continue;
            }
            chapterCounts.merge(chapterId, 1L, Long::sum);
            if (Integer.valueOf(QUIZ_ANSWER_CORRECT).equals(answer.getIsCorrect())) {
                chapterCorrect.merge(chapterId, 1L, Long::sum);
            }
        }

        if (chapterCounts.isEmpty()) {
            return weakDomains;
        }

        Set<Long> chapterIds = chapterCounts.keySet();
        List<Chapter> chapters = chapterMapper.selectList(new LambdaQueryWrapper<Chapter>()
                .in(Chapter::getId, chapterIds)
                .select(Chapter::getId, Chapter::getTitle));
        Map<Long, String> chapterTitleMap = chapters.stream()
                .filter(c -> c.getTitle() != null)
                .collect(Collectors.toMap(Chapter::getId, Chapter::getTitle, (a, b) -> a));

        chapterCounts.forEach((chapterId, total) -> {
            long correct = chapterCorrect.getOrDefault(chapterId, 0L);
            double rate = (double) correct / total;
            if (rate < WEAK_THRESHOLD) {
                weakDomains.add(DynamicProfileResponse.WeakDomain.builder()
                        .type("CHAPTER")
                        .name(chapterTitleMap.getOrDefault(chapterId, "章节" + chapterId))
                        .accuracy(rate)
                        .build());
            }
        });

        return weakDomains;
    }
}
