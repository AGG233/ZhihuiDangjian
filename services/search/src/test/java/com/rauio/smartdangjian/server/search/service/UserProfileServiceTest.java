package com.rauio.smartdangjian.server.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.annotation.Cacheable;

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

@ExtendWith(MockitoExtension.class)
@DisplayName("UserProfileService 用户画像构建")
class UserProfileServiceTest {

    @Mock
    private UserLearningRecordService learningRecordService;

    @Mock
    private UserChapterProgressService chapterProgressService;

    @Mock
    private UserQuizAnswerService quizAnswerService;

    @Mock
    private QuizService quizService;

    @Mock
    private ChapterService chapterService;

    @Mock
    private CourseService courseService;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserProfileService userProfileService;

    // ==================== getProfile ====================

    @Test
    @DisplayName("getProfile 缓存启用 sync，避免用户画像并发击穿")
    void getProfileCacheUsesSync() throws NoSuchMethodException {
        Method method = UserProfileService.class.getMethod("getProfile", String.class);

        assertThat(method.getAnnotation(Cacheable.class).sync()).isTrue();
    }

    @Test
    @DisplayName("有完整学习数据时返回所有画像统计")
    void getProfileWithCompleteDataReturnsFullProfile() {
        String userId = "1";

        LearningRecordSummaryDto record1 = new LearningRecordSummaryDto(1L, 1L, 600);
        LearningRecordSummaryDto record2 = new LearningRecordSummaryDto(1L, 1L, 300);
        doReturn(List.of(record1, record2)).when(learningRecordService).listRecordSummariesByUserId(1L);

        ChapterProgressSummaryDto progress = new ChapterProgressSummaryDto(1L, 1L, 100, "completed");
        doReturn(List.of(progress)).when(chapterProgressService).listProgressSummariesByUserId(1L);
        doReturn(1L).when(chapterProgressService).countCompletedByUserId(1L);
        doReturn(List.of(1L)).when(chapterService).listCourseIdsByChapterIds(Collections.singleton(1L));
        doReturn(List.of(1L)).when(courseService).listTopCategoryIdsByCourseIds(List.of(1L), 5);

        doReturn(Collections.emptyList()).when(quizAnswerService).listAnswerSummariesByUserId(1L);

        UserProfileResponse profile = userProfileService.getProfile(userId);

        assertThat(profile.getUserId()).isEqualTo(userId);
        assertThat(profile.getLearning()).isNotNull();
        assertThat(profile.getLearning().getTotalDuration()).isEqualTo(900);
        assertThat(profile.getLearning().getAvgDuration()).isEqualTo(450.0);
        assertThat(profile.getLearning().getTotalRecords()).isEqualTo(2);
        assertThat(profile.getLearning().getCompletedChapters()).isEqualTo(1);

        assertThat(profile.getKnowledge()).isNotNull();
        assertThat(profile.getKnowledge().getAvgProgress()).isEqualTo(100.0);
        assertThat(profile.getKnowledge().getCompletionRate()).isEqualTo(1.0);

        assertThat(profile.getInterestCategoryIds()).containsExactly(1L);

        assertThat(profile.getQuiz()).isNotNull();
        assertThat(profile.getQuiz().getTotalAnswers()).isZero();
    }

    @Test
    @DisplayName("无学习记录时返回空统计")
    void getProfileWithNoDataReturnsEmptyStats() {
        String userId = "1";

        doReturn(Collections.emptyList()).when(learningRecordService).listRecordSummariesByUserId(1L);
        doReturn(Collections.emptyList()).when(chapterProgressService).listProgressSummariesByUserId(1L);
        doReturn(0L).when(chapterProgressService).countCompletedByUserId(1L);
        doReturn(Collections.emptyList()).when(quizAnswerService).listAnswerSummariesByUserId(1L);

        UserProfileResponse profile = userProfileService.getProfile(userId);

        assertThat(profile.getUserId()).isEqualTo(userId);
        assertThat(profile.getLearning().getTotalDuration()).isZero();
        assertThat(profile.getLearning().getAvgDuration()).isZero();
        assertThat(profile.getLearning().getTotalRecords()).isZero();
        assertThat(profile.getKnowledge().getAvgProgress()).isZero();
        assertThat(profile.getKnowledge().getCompletionRate()).isZero();
        assertThat(profile.getInterestCategoryIds()).isEmpty();
        assertThat(profile.getQuiz().getTotalAnswers()).isZero();
    }

    @Test
    @DisplayName("有答题数据时包含按难度统计的正确率")
    void getProfileWithQuizDataIncludesDifficultyBreakdown() {
        String userId = "1";

        doReturn(Collections.emptyList()).when(learningRecordService).listRecordSummariesByUserId(1L);
        doReturn(Collections.emptyList()).when(chapterProgressService).listProgressSummariesByUserId(1L);
        doReturn(0L).when(chapterProgressService).countCompletedByUserId(1L);

        UserQuizAnswerSummaryDto a1 = new UserQuizAnswerSummaryDto(1L, 1L, 1, 30);
        UserQuizAnswerSummaryDto a2 = new UserQuizAnswerSummaryDto(1L, 2L, 0, 60);
        UserQuizAnswerSummaryDto a3 = new UserQuizAnswerSummaryDto(1L, 3L, 1, 45);
        doReturn(List.of(a1, a2, a3)).when(quizAnswerService).listAnswerSummariesByUserId(1L);
        doReturn(Map.of(1L, "easy", 2L, "easy", 3L, "medium"))
                .when(quizService)
                .getDifficultyMapByIds(Set.of(1L, 2L, 3L));

        UserProfileResponse profile = userProfileService.getProfile(userId);

        assertThat(profile.getQuiz().getTotalAnswers()).isEqualTo(3);
        assertThat(profile.getQuiz().getCorrectCount()).isEqualTo(2);
        assertThat(profile.getQuiz().getCorrectRate()).isEqualTo(2.0 / 3.0);
        assertThat(profile.getQuiz().getAvgTimeSpent()).isEqualTo((30.0 + 60.0 + 45.0) / 3.0);
        assertThat(profile.getQuiz().getByDifficulty()).containsEntry("easy", 0.5);
        assertThat(profile.getQuiz().getByDifficulty()).containsEntry("medium", 1.0);
    }

    @Test
    @DisplayName("学习记录duration为null时处理为0")
    void getProfileHandlesNullDuration() {
        String userId = "1";

        LearningRecordSummaryDto record = new LearningRecordSummaryDto(1L, 1L, null);
        doReturn(List.of(record)).when(learningRecordService).listRecordSummariesByUserId(1L);
        doReturn(Collections.emptyList()).when(chapterProgressService).listProgressSummariesByUserId(1L);
        doReturn(0L).when(chapterProgressService).countCompletedByUserId(1L);
        doReturn(Collections.emptyList()).when(quizAnswerService).listAnswerSummariesByUserId(1L);

        UserProfileResponse profile = userProfileService.getProfile(userId);

        assertThat(profile.getLearning().getTotalDuration()).isZero();
        assertThat(profile.getLearning().getAvgDuration()).isZero();
    }

    @Test
    @DisplayName("进度为null的章节不影响平均进度且不计入薄弱章节")
    void getProfileHandlesNullProgress() {
        String userId = "1";

        doReturn(Collections.emptyList()).when(learningRecordService).listRecordSummariesByUserId(1L);
        doReturn(0L).when(chapterProgressService).countCompletedByUserId(1L);
        doReturn(Collections.emptyList()).when(quizAnswerService).listAnswerSummariesByUserId(1L);

        ChapterProgressSummaryDto p1 = new ChapterProgressSummaryDto(1L, 1L, null, "in_progress");
        ChapterProgressSummaryDto p2 = new ChapterProgressSummaryDto(1L, 2L, 60, "in_progress");
        doReturn(List.of(p1, p2)).when(chapterProgressService).listProgressSummariesByUserId(1L);

        UserProfileResponse profile = userProfileService.getProfile(userId);

        assertThat(profile.getKnowledge().getAvgProgress()).isEqualTo(30.0);
    }

    @Test
    @DisplayName("无学习记录时不会查询章节和分类")
    void getProfileNoRecordsSkipsChapterAndCategoryQueries() {
        String userId = "1";

        doReturn(Collections.emptyList()).when(learningRecordService).listRecordSummariesByUserId(1L);
        doReturn(Collections.emptyList()).when(chapterProgressService).listProgressSummariesByUserId(1L);
        doReturn(0L).when(chapterProgressService).countCompletedByUserId(1L);
        doReturn(Collections.emptyList()).when(quizAnswerService).listAnswerSummariesByUserId(1L);

        userProfileService.getProfile(userId);

        verify(chapterService, never()).listCourseIdsByChapterIds(Set.of());
        verify(courseService, never()).listTopCategoryIdsByCourseIds(List.of(), 5);
    }

    @Test
    @DisplayName("学习记录有null deviceType时被preferredDevice过滤掉")
    void getProfileHandlesNullDeviceType() {
        String userId = "1";

        LearningRecordSummaryDto r1 = new LearningRecordSummaryDto(1L, 1L, 100);
        LearningRecordSummaryDto r2 = new LearningRecordSummaryDto(1L, 1L, 200);
        doReturn(List.of(r1, r2)).when(learningRecordService).listRecordSummariesByUserId(1L);
        doReturn(Collections.emptyList()).when(chapterProgressService).listProgressSummariesByUserId(1L);
        doReturn(0L).when(chapterProgressService).countCompletedByUserId(1L);
        doReturn(Collections.emptyList()).when(quizAnswerService).listAnswerSummariesByUserId(1L);

        UserProfileResponse profile = userProfileService.getProfile(userId);

        assertThat(profile.getLearning().getTotalDuration()).isEqualTo(300);
    }

    @Test
    @DisplayName("进度<50的章节计入薄弱章节列表")
    void getProfileWithWeakChapters() {
        String userId = "1";

        doReturn(Collections.emptyList()).when(learningRecordService).listRecordSummariesByUserId(1L);
        doReturn(0L).when(chapterProgressService).countCompletedByUserId(1L);
        doReturn(Collections.emptyList()).when(quizAnswerService).listAnswerSummariesByUserId(1L);

        ChapterProgressSummaryDto weak = new ChapterProgressSummaryDto(1L, 10L, 30, "in_progress");
        ChapterProgressSummaryDto strong = new ChapterProgressSummaryDto(1L, 20L, 80, "in_progress");
        doReturn(List.of(weak, strong)).when(chapterProgressService).listProgressSummariesByUserId(1L);

        UserProfileResponse profile = userProfileService.getProfile(userId);

        assertThat(profile.getKnowledge().getAvgProgress()).isEqualTo(55.0);
        assertThat(profile.getKnowledge().getCompletionRate()).isZero();
    }

    @Test
    @DisplayName("学习记录chapterId全为null时interestCategoryIds返回空")
    void getProfileWithNullChapterIdsInRecords() {
        String userId = "1";

        LearningRecordSummaryDto record = new LearningRecordSummaryDto(1L, null, 100);
        doReturn(List.of(record)).when(learningRecordService).listRecordSummariesByUserId(1L);
        doReturn(Collections.emptyList()).when(chapterProgressService).listProgressSummariesByUserId(1L);
        doReturn(0L).when(chapterProgressService).countCompletedByUserId(1L);
        doReturn(Collections.emptyList()).when(quizAnswerService).listAnswerSummariesByUserId(1L);

        UserProfileResponse profile = userProfileService.getProfile(userId);

        assertThat(profile.getInterestCategoryIds()).isEmpty();
    }

    @Test
    @DisplayName("答题有时间为null的记录时跳过并计算平均值")
    void getProfileWithNullTimeSpent() {
        String userId = "1";

        doReturn(Collections.emptyList()).when(learningRecordService).listRecordSummariesByUserId(1L);
        doReturn(Collections.emptyList()).when(chapterProgressService).listProgressSummariesByUserId(1L);
        doReturn(0L).when(chapterProgressService).countCompletedByUserId(1L);

        UserQuizAnswerSummaryDto a1 = new UserQuizAnswerSummaryDto(1L, 1L, 1, 30);
        UserQuizAnswerSummaryDto a2 = new UserQuizAnswerSummaryDto(1L, 1L, 1, null);
        doReturn(List.of(a1, a2)).when(quizAnswerService).listAnswerSummariesByUserId(1L);
        doReturn(Map.of(1L, "easy")).when(quizService).getDifficultyMapByIds(Set.of(1L));

        UserProfileResponse profile = userProfileService.getProfile(userId);

        assertThat(profile.getQuiz().getTotalAnswers()).isEqualTo(2);
        assertThat(profile.getQuiz().getAvgTimeSpent()).isEqualTo(30.0);
    }

    @Test
    @DisplayName("答题记录quizId全为null时跳过难度分组")
    void getProfileWithNullQuizIds() {
        String userId = "1";

        doReturn(Collections.emptyList()).when(learningRecordService).listRecordSummariesByUserId(1L);
        doReturn(Collections.emptyList()).when(chapterProgressService).listProgressSummariesByUserId(1L);
        doReturn(0L).when(chapterProgressService).countCompletedByUserId(1L);

        UserQuizAnswerSummaryDto a1 = new UserQuizAnswerSummaryDto(1L, null, 1, 30);
        UserQuizAnswerSummaryDto a2 = new UserQuizAnswerSummaryDto(1L, null, 0, 60);
        doReturn(List.of(a1, a2)).when(quizAnswerService).listAnswerSummariesByUserId(1L);

        // quizService should NOT be called because quizIds set is empty
        UserProfileResponse profile = userProfileService.getProfile(userId);

        assertThat(profile.getQuiz().getTotalAnswers()).isEqualTo(2);
        assertThat(profile.getQuiz().getByDifficulty()).isEmpty();
        verify(quizService, never()).getDifficultyMapByIds(Set.of());
    }

    @Test
    @DisplayName("测验有null难度时过滤掉后不影响分组统计")
    void getProfileWithNullDifficultyQuiz() {
        String userId = "1";

        doReturn(Collections.emptyList()).when(learningRecordService).listRecordSummariesByUserId(1L);
        doReturn(Collections.emptyList()).when(chapterProgressService).listProgressSummariesByUserId(1L);
        doReturn(0L).when(chapterProgressService).countCompletedByUserId(1L);

        UserQuizAnswerSummaryDto a1 = new UserQuizAnswerSummaryDto(1L, 1L, 1, 30);
        UserQuizAnswerSummaryDto a2 = new UserQuizAnswerSummaryDto(1L, 2L, 0, 60);
        doReturn(List.of(a1, a2)).when(quizAnswerService).listAnswerSummariesByUserId(1L);
        doReturn(Map.of(1L, "easy")).when(quizService).getDifficultyMapByIds(Set.of(1L, 2L));

        UserProfileResponse profile = userProfileService.getProfile(userId);

        assertThat(profile.getQuiz().getTotalAnswers()).isEqualTo(2);
        // q2 should be filtered out from difficulty map, so only q1's answer contributes
        assertThat(profile.getQuiz().getByDifficulty()).containsOnlyKeys("easy");
    }

    // ==================== getCurrentUserProfile ====================

    @Test
    @DisplayName("getCurrentUserProfile 获取当前用户ID后委托 getProfile")
    void getCurrentUserProfileDelegatesToGetProfile() {
        doReturn("1").when(userService).getCurrentUserId();

        doReturn(Collections.emptyList()).when(learningRecordService).listRecordSummariesByUserId(1L);
        doReturn(Collections.emptyList()).when(chapterProgressService).listProgressSummariesByUserId(1L);
        doReturn(0L).when(chapterProgressService).countCompletedByUserId(1L);
        doReturn(Collections.emptyList()).when(quizAnswerService).listAnswerSummariesByUserId(1L);

        UserProfileResponse profile = userProfileService.getCurrentUserProfile();

        assertThat(profile.getUserId()).isEqualTo("1");
        verify(userService).getCurrentUserId();
    }
}
