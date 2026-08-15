package com.rauio.smartdangjian.server.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.content.comment.mapper.CommentMapper;
import com.rauio.smartdangjian.server.content.comment.mapper.LikeRecordMapper;
import com.rauio.smartdangjian.server.content.comment.pojo.entity.Comment;
import com.rauio.smartdangjian.server.content.comment.pojo.entity.LikeRecord;
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
import com.rauio.smartdangjian.server.user.mapper.UserSimilarityMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.UserSimilarity;
import com.rauio.smartdangjian.server.user.service.UserService;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserProfileService 用户画像构建")
class UserProfileServiceTest {

    @BeforeAll
    static void initMybatisPlus() {
        MybatisConfiguration config = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(config, "");
        TableInfoHelper.initTableInfo(assistant, Chapter.class);
        TableInfoHelper.initTableInfo(assistant, CategoryCourse.class);
        TableInfoHelper.initTableInfo(assistant, UserLearningRecord.class);
        TableInfoHelper.initTableInfo(assistant, UserChapterProgress.class);
        TableInfoHelper.initTableInfo(assistant, UserQuizAnswer.class);
        TableInfoHelper.initTableInfo(assistant, Quiz.class);
        TableInfoHelper.initTableInfo(assistant, Comment.class);
        TableInfoHelper.initTableInfo(assistant, LikeRecord.class);
        TableInfoHelper.initTableInfo(assistant, UserSimilarity.class);
    }

    @BeforeEach
    void stubInteractionAndCfDefaults() {
        // 互动统计与协同过滤的默认空数据，避免未 stub 的 mock 返回 null
        lenient().when(commentMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        lenient().when(commentMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(Collections.emptyList());
        lenient()
                .when(likeRecordMapper.selectCount(any(LambdaQueryWrapper.class)))
                .thenReturn(0L);
        lenient()
                .when(likeRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(Collections.emptyList());
        lenient()
                .when(userSimilarityMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(new Page<>(1, 3));
    }

    @Mock
    private UserLearningRecordMapper learningRecordMapper;

    @Mock
    private UserChapterProgressMapper chapterProgressMapper;

    @Mock
    private UserQuizAnswerMapper quizAnswerMapper;

    @Mock
    private QuizMapper quizMapper;

    @Mock
    private ChapterMapper chapterMapper;

    @Mock
    private CategoryCourseMapper categoryCourseMapper;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private LikeRecordMapper likeRecordMapper;

    @Mock
    private UserSimilarityMapper userSimilarityMapper;

    @Mock
    private UserService userService;

    @Spy
    @InjectMocks
    private UserProfileService userProfileService;

    // ==================== getProfile ====================

    @Test
    @DisplayName("有完整学习数据时返回所有画像统计")
    void getProfileWithCompleteDataReturnsFullProfile() {
        String userId = "user-1";

        UserLearningRecord record1 = UserLearningRecord.builder()
                .userId(1L)
                .chapterId(1L)
                .duration(600)
                .deviceType("web")
                .build();
        UserLearningRecord record2 = UserLearningRecord.builder()
                .userId(1L)
                .chapterId(1L)
                .duration(300)
                .deviceType("web")
                .build();
        doReturn(List.of(record1, record2)).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));

        UserChapterProgress progress = UserChapterProgress.builder()
                .userId(1L)
                .chapterId(1L)
                .progress(100)
                .status("completed")
                .build();
        doReturn(List.of(progress)).when(chapterProgressMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(1L).when(chapterProgressMapper).selectCount(any(LambdaQueryWrapper.class));

        Chapter chapter = Chapter.builder().id(1L).courseId(1L).build();
        Chapter chapter2 = Chapter.builder().id(2L).courseId(1L).build();
        doReturn(List.of(chapter, chapter2)).when(chapterMapper).selectList(any(LambdaQueryWrapper.class));

        CategoryCourse cc = CategoryCourse.builder().courseId(1L).categoryId(1L).build();
        CategoryCourse cc2 =
                CategoryCourse.builder().courseId(1L).categoryId(1L).build();
        doReturn(List.of(cc, cc2)).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));

        doReturn(Collections.emptyList()).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));

        UserProfileResponse profile = userProfileService.getProfile(userId);

        assertThat(profile.getUserId()).isEqualTo(userId);
        assertThat(profile.getLearning()).isNotNull();
        assertThat(profile.getLearning().getTotalDuration()).isEqualTo(900);
        assertThat(profile.getLearning().getAvgDuration()).isEqualTo(450.0);
        assertThat(profile.getLearning().getTotalRecords()).isEqualTo(2);
        assertThat(profile.getLearning().getCompletedChapters()).isEqualTo(1);
        assertThat(profile.getLearning().getPreferredDevice()).isEqualTo("web");

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
        String userId = "user-1";

        doReturn(Collections.emptyList()).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(chapterProgressMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(0L).when(chapterProgressMapper).selectCount(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));

        UserProfileResponse profile = userProfileService.getProfile(userId);

        assertThat(profile.getUserId()).isEqualTo(userId);
        assertThat(profile.getLearning().getTotalDuration()).isZero();
        assertThat(profile.getLearning().getAvgDuration()).isZero();
        assertThat(profile.getLearning().getTotalRecords()).isZero();
        assertThat(profile.getLearning().getPreferredDevice()).isNull();
        assertThat(profile.getKnowledge().getAvgProgress()).isZero();
        assertThat(profile.getKnowledge().getCompletionRate()).isZero();
        assertThat(profile.getInterestCategoryIds()).isEmpty();
        assertThat(profile.getQuiz().getTotalAnswers()).isZero();
    }

    @Test
    @DisplayName("有答题数据时包含按难度统计的正确率")
    void getProfileWithQuizDataIncludesDifficultyBreakdown() {
        String userId = "user-1";

        doReturn(Collections.emptyList()).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(chapterProgressMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(0L).when(chapterProgressMapper).selectCount(any(LambdaQueryWrapper.class));

        UserQuizAnswer a1 = UserQuizAnswer.builder()
                .userId(1L)
                .quizId(1L)
                .isCorrect(1)
                .timeSpent(30)
                .build();
        UserQuizAnswer a2 = UserQuizAnswer.builder()
                .userId(1L)
                .quizId(2L)
                .isCorrect(0)
                .timeSpent(60)
                .build();
        UserQuizAnswer a3 = UserQuizAnswer.builder()
                .userId(1L)
                .quizId(3L)
                .isCorrect(1)
                .timeSpent(45)
                .build();
        doReturn(List.of(a1, a2, a3)).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));

        Quiz q1 = Quiz.builder().id(1L).difficulty("easy").build();
        Quiz q2 = Quiz.builder().id(2L).difficulty("easy").build();
        Quiz q3 = Quiz.builder().id(3L).difficulty("medium").build();
        doReturn(List.of(q1, q2, q3)).when(quizMapper).selectList(any(LambdaQueryWrapper.class));

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
        String userId = "user-1";

        UserLearningRecord record = UserLearningRecord.builder()
                .userId(1L)
                .chapterId(1L)
                .duration(null)
                .deviceType("mobile")
                .build();
        doReturn(List.of(record)).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(chapterProgressMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(0L).when(chapterProgressMapper).selectCount(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));

        UserProfileResponse profile = userProfileService.getProfile(userId);

        assertThat(profile.getLearning().getTotalDuration()).isZero();
        assertThat(profile.getLearning().getAvgDuration()).isZero();
    }

    @Test
    @DisplayName("进度为null的章节不影响平均进度且不计入薄弱章节")
    void getProfileHandlesNullProgress() {
        String userId = "user-1";

        doReturn(Collections.emptyList()).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(0L).when(chapterProgressMapper).selectCount(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));

        UserChapterProgress p1 = UserChapterProgress.builder()
                .userId(1L)
                .chapterId(1L)
                .progress(null)
                .status("in_progress")
                .build();
        UserChapterProgress p2 = UserChapterProgress.builder()
                .userId(1L)
                .chapterId(2L)
                .progress(60)
                .status("in_progress")
                .build();
        doReturn(List.of(p1, p2)).when(chapterProgressMapper).selectList(any(LambdaQueryWrapper.class));

        UserProfileResponse profile = userProfileService.getProfile(userId);

        assertThat(profile.getKnowledge().getAvgProgress()).isEqualTo(30.0);
        assertThat(profile.getKnowledge().getWeakChapterIds()).isEmpty();
    }

    @Test
    @DisplayName("无学习记录时不会查询章节和分类")
    void getProfileNoRecordsSkipsChapterAndCategoryQueries() {
        String userId = "user-1";

        doReturn(Collections.emptyList()).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(chapterProgressMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(0L).when(chapterProgressMapper).selectCount(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));

        userProfileService.getProfile(userId);

        verify(chapterMapper, never()).selectList(any(LambdaQueryWrapper.class));
        verify(categoryCourseMapper, never()).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("学习记录有null deviceType时被preferredDevice过滤掉")
    void getProfileHandlesNullDeviceType() {
        String userId = "user-1";

        UserLearningRecord r1 = UserLearningRecord.builder()
                .userId(1L)
                .chapterId(1L)
                .duration(100)
                .deviceType("mobile")
                .build();
        UserLearningRecord r2 = UserLearningRecord.builder()
                .userId(1L)
                .chapterId(1L)
                .duration(200)
                .deviceType(null)
                .build();
        doReturn(List.of(r1, r2)).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(chapterProgressMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(0L).when(chapterProgressMapper).selectCount(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));

        UserProfileResponse profile = userProfileService.getProfile(userId);

        assertThat(profile.getLearning().getPreferredDevice()).isEqualTo("mobile");
        assertThat(profile.getLearning().getTotalDuration()).isEqualTo(300);
    }

    @Test
    @DisplayName("进度<50的章节计入薄弱章节列表")
    void getProfileWithWeakChapters() {
        String userId = "user-1";

        doReturn(Collections.emptyList()).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(0L).when(chapterProgressMapper).selectCount(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));

        UserChapterProgress weak = UserChapterProgress.builder()
                .userId(1L)
                .chapterId(10L)
                .progress(30)
                .status("in_progress")
                .build();
        UserChapterProgress strong = UserChapterProgress.builder()
                .userId(1L)
                .chapterId(20L)
                .progress(80)
                .status("in_progress")
                .build();
        doReturn(List.of(weak, strong)).when(chapterProgressMapper).selectList(any(LambdaQueryWrapper.class));

        UserProfileResponse profile = userProfileService.getProfile(userId);

        assertThat(profile.getKnowledge().getWeakChapterIds()).containsExactly(10L);
    }

    @Test
    @DisplayName("学习记录chapterId全为null时interestCategoryIds返回空")
    void getProfileWithNullChapterIdsInRecords() {
        String userId = "user-1";

        UserLearningRecord record = UserLearningRecord.builder()
                .userId(1L)
                .chapterId(null)
                .duration(100)
                .build();
        doReturn(List.of(record)).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(chapterProgressMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(0L).when(chapterProgressMapper).selectCount(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));

        UserProfileResponse profile = userProfileService.getProfile(userId);

        assertThat(profile.getInterestCategoryIds()).isEmpty();
    }

    @Test
    @DisplayName("答题有时间为null的记录时跳过并计算平均值")
    void getProfileWithNullTimeSpent() {
        String userId = "user-1";

        doReturn(Collections.emptyList()).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(chapterProgressMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(0L).when(chapterProgressMapper).selectCount(any(LambdaQueryWrapper.class));

        UserQuizAnswer a1 = UserQuizAnswer.builder()
                .userId(1L)
                .quizId(1L)
                .isCorrect(1)
                .timeSpent(30)
                .build();
        UserQuizAnswer a2 = UserQuizAnswer.builder()
                .userId(1L)
                .quizId(1L)
                .isCorrect(1)
                .timeSpent(null)
                .build();
        doReturn(List.of(a1, a2)).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));

        Quiz q1 = Quiz.builder().id(1L).difficulty("easy").build();
        doReturn(List.of(q1)).when(quizMapper).selectList(any(LambdaQueryWrapper.class));

        UserProfileResponse profile = userProfileService.getProfile(userId);

        assertThat(profile.getQuiz().getTotalAnswers()).isEqualTo(2);
        assertThat(profile.getQuiz().getAvgTimeSpent()).isEqualTo(30.0);
    }

    @Test
    @DisplayName("答题记录quizId全为null时跳过难度分组")
    void getProfileWithNullQuizIds() {
        String userId = "user-1";

        doReturn(Collections.emptyList()).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(chapterProgressMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(0L).when(chapterProgressMapper).selectCount(any(LambdaQueryWrapper.class));

        UserQuizAnswer a1 = UserQuizAnswer.builder()
                .userId(1L)
                .quizId(null)
                .isCorrect(1)
                .timeSpent(30)
                .build();
        UserQuizAnswer a2 = UserQuizAnswer.builder()
                .userId(1L)
                .quizId(null)
                .isCorrect(0)
                .timeSpent(60)
                .build();
        doReturn(List.of(a1, a2)).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));

        // quizMapper should NOT be called because quizIds set is empty
        UserProfileResponse profile = userProfileService.getProfile(userId);

        assertThat(profile.getQuiz().getTotalAnswers()).isEqualTo(2);
        assertThat(profile.getQuiz().getByDifficulty()).isEmpty();
        verify(quizMapper, never()).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("测验有null难度时过滤掉后不影响分组统计")
    void getProfileWithNullDifficultyQuiz() {
        String userId = "user-1";

        doReturn(Collections.emptyList()).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(chapterProgressMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(0L).when(chapterProgressMapper).selectCount(any(LambdaQueryWrapper.class));

        UserQuizAnswer a1 = UserQuizAnswer.builder()
                .userId(1L)
                .quizId(1L)
                .isCorrect(1)
                .timeSpent(30)
                .build();
        UserQuizAnswer a2 = UserQuizAnswer.builder()
                .userId(1L)
                .quizId(2L)
                .isCorrect(0)
                .timeSpent(60)
                .build();
        doReturn(List.of(a1, a2)).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));

        Quiz q1 = Quiz.builder().id(1L).difficulty("easy").build();
        Quiz q2 = Quiz.builder().id(2L).difficulty(null).build();
        doReturn(List.of(q1, q2)).when(quizMapper).selectList(any(LambdaQueryWrapper.class));

        UserProfileResponse profile = userProfileService.getProfile(userId);

        assertThat(profile.getQuiz().getTotalAnswers()).isEqualTo(2);
        // q2 should be filtered out from difficulty map, so only q1's answer contributes
        assertThat(profile.getQuiz().getByDifficulty()).containsOnlyKeys("easy");
    }

    // ==================== getCurrentUserProfile ====================

    @Test
    @DisplayName("getCurrentUserProfile 获取当前用户ID后委托 getProfile")
    void getCurrentUserProfileDelegatesToGetProfile() {
        doReturn("current-user").when(userService).getCurrentUserId();

        doReturn(Collections.emptyList()).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(chapterProgressMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(0L).when(chapterProgressMapper).selectCount(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));

        UserProfileResponse profile = userProfileService.getCurrentUserProfile();

        assertThat(profile.getUserId()).isEqualTo("current-user");
        verify(userService).getCurrentUserId();
    }

    // ==================== buildDynamicProfile ====================

    @Test
    @DisplayName("有完整数据时返回热点标签Top3、8周趋势与薄弱知识域")
    void buildDynamicProfileWithDataReturnsTagsTrendAndWeakDomains() {
        String userId = "user-1";
        LocalDateTime now = LocalDateTime.now();

        UserLearningRecord r1 = UserLearningRecord.builder()
                .userId(1L)
                .chapterId(1L)
                .duration(100)
                .startTime(now)
                .createdAt(now)
                .build();
        UserLearningRecord r2 = UserLearningRecord.builder()
                .userId(1L)
                .chapterId(1L)
                .duration(100)
                .startTime(now)
                .createdAt(now)
                .build();
        UserLearningRecord r3 = UserLearningRecord.builder()
                .userId(1L)
                .chapterId(2L)
                .duration(100)
                .startTime(now)
                .createdAt(now)
                .build();
        UserLearningRecord r4 = UserLearningRecord.builder()
                .userId(1L)
                .chapterId(3L)
                .duration(100)
                .startTime(now)
                .createdAt(now)
                .build();
        doReturn(List.of(r1, r2, r3, r4)).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));

        Chapter c1 = Chapter.builder().id(1L).title("党章学习").build();
        Chapter c2 = Chapter.builder().id(2L).title("党史学习").build();
        Chapter c3 = Chapter.builder().id(3L).title("组织建设").build();
        Chapter c20 = Chapter.builder().id(20L).title("薄弱章节").build();
        doReturn(List.of(c1, c2, c3, c20)).when(chapterMapper).selectList(any(LambdaQueryWrapper.class));

        UserQuizAnswer a1 = UserQuizAnswer.builder()
                .userId(1L)
                .quizId(1L)
                .isCorrect(1)
                .answerTime(now)
                .build();
        UserQuizAnswer a2 = UserQuizAnswer.builder()
                .userId(1L)
                .quizId(2L)
                .isCorrect(0)
                .answerTime(now)
                .build();
        UserQuizAnswer a3 = UserQuizAnswer.builder()
                .userId(1L)
                .quizId(2L)
                .isCorrect(0)
                .answerTime(now)
                .build();
        doReturn(List.of(a1, a2, a3)).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));

        Quiz q1 = Quiz.builder().id(1L).difficulty("easy").chapterId(10L).build();
        Quiz q2 = Quiz.builder().id(2L).difficulty("hard").chapterId(20L).build();
        doReturn(List.of(q1, q2)).when(quizMapper).selectList(any(LambdaQueryWrapper.class));

        DynamicProfileResponse profile = userProfileService.buildDynamicProfile(userId);

        assertThat(profile.getHotTags()).hasSize(3);
        assertThat(profile.getHotTags().get(0).getTag()).isEqualTo("党章学习");
        assertThat(profile.getHotTags().get(0).getCount()).isEqualTo(2L);

        assertThat(profile.getGrowthTrend()).hasSize(8);
        assertThat(profile.getGrowthTrend().get(7).getStudyDuration()).isEqualTo(400);
        assertThat(profile.getGrowthTrend().get(7).getQuizAccuracy()).isEqualTo(1.0 / 3.0);

        assertThat(profile.getWeakDomains()).isNotEmpty();
        assertThat(profile.getWeakDomains())
                .anyMatch(w -> "DIFFICULTY".equals(w.getType()) && "hard".equals(w.getName()));
        assertThat(profile.getWeakDomains()).anyMatch(w -> "CHAPTER".equals(w.getType()) && "薄弱章节".equals(w.getName()));
    }

    @Test
    @DisplayName("无学习记录时动态画像返回空结构而非异常")
    void buildDynamicProfileNoDataReturnsEmptyStructures() {
        String userId = "user-1";

        doReturn(Collections.emptyList()).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));

        DynamicProfileResponse profile = userProfileService.buildDynamicProfile(userId);

        assertThat(profile.getHotTags()).isEmpty();
        assertThat(profile.getGrowthTrend()).hasSize(8);
        assertThat(profile.getGrowthTrend().get(7).getStudyDuration()).isZero();
        assertThat(profile.getGrowthTrend().get(7).getQuizAccuracy()).isZero();
        assertThat(profile.getWeakDomains()).isEmpty();
    }

    // ==================== getLearningSummary ====================

    @Test
    @DisplayName("getLearningSummary 组合学习统计与答题统计")
    void getLearningSummaryCombinesStats() {
        String userId = "user-1";

        UserLearningRecord r1 =
                UserLearningRecord.builder().userId(1L).duration(600).build();
        UserLearningRecord r2 =
                UserLearningRecord.builder().userId(1L).duration(300).build();
        doReturn(List.of(r1, r2)).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(1L).when(chapterProgressMapper).selectCount(any(LambdaQueryWrapper.class));

        UserChapterProgress p1 = UserChapterProgress.builder()
                .userId(1L)
                .progress(100)
                .status("completed")
                .build();
        UserChapterProgress p2 = UserChapterProgress.builder()
                .userId(1L)
                .progress(60)
                .status("in_progress")
                .build();
        doReturn(List.of(p1, p2)).when(chapterProgressMapper).selectList(any(LambdaQueryWrapper.class));

        UserQuizAnswer a1 =
                UserQuizAnswer.builder().userId(1L).quizId(1L).isCorrect(1).build();
        UserQuizAnswer a2 =
                UserQuizAnswer.builder().userId(1L).quizId(2L).isCorrect(0).build();
        UserQuizAnswer a3 =
                UserQuizAnswer.builder().userId(1L).quizId(3L).isCorrect(1).build();
        doReturn(List.of(a1, a2, a3)).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));

        Quiz q1 = Quiz.builder().id(1L).difficulty("easy").build();
        Quiz q2 = Quiz.builder().id(2L).difficulty("easy").build();
        Quiz q3 = Quiz.builder().id(3L).difficulty("medium").build();
        doReturn(List.of(q1, q2, q3)).when(quizMapper).selectList(any(LambdaQueryWrapper.class));

        LearningSummaryResponse summary = userProfileService.getLearningSummary(userId);

        assertThat(summary.getTheory().getTotalDuration()).isEqualTo(900);
        assertThat(summary.getTheory().getCompletionRate()).isEqualTo(0.5);
        assertThat(summary.getPolicyComprehension().getAvgCorrectRate()).isEqualTo(2.0 / 3.0);
        assertThat(summary.getPolicyComprehension().getTotalAnswers()).isEqualTo(3);
    }

    @Test
    @DisplayName("getLearningSummary 无数据时返回零值")
    void getLearningSummaryNoDataReturnsZeros() {
        String userId = "user-1";

        doReturn(Collections.emptyList()).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(chapterProgressMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(0L).when(chapterProgressMapper).selectCount(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));

        LearningSummaryResponse summary = userProfileService.getLearningSummary(userId);

        assertThat(summary.getTheory().getTotalDuration()).isZero();
        assertThat(summary.getTheory().getCompletionRate()).isZero();
        assertThat(summary.getPolicyComprehension().getAvgCorrectRate()).isZero();
        assertThat(summary.getPolicyComprehension().getTotalAnswers()).isZero();
    }

    // ==================== buildDynamicProfile 边界分支 ====================

    @Test
    @DisplayName("热点标签：章节标题为null被过滤且超过3个标签时截断Top3")
    void buildDynamicProfileHotTagsFilterNullTitleAndLimitTop3() {
        String userId = "user-1";
        LocalDateTime now = LocalDateTime.now();

        List<UserLearningRecord> hotRecords = new ArrayList<>();
        for (int i = 0; i < 4; i++) hotRecords.add(record(1L, now, now, 100));
        hotRecords.add(record(2L, now, now, 100));
        for (int i = 0; i < 3; i++) hotRecords.add(record(3L, now, now, 100));
        for (int i = 0; i < 2; i++) hotRecords.add(record(4L, now, now, 100));
        hotRecords.add(record(5L, now, now, 100));
        doReturn(hotRecords, Collections.emptyList())
                .when(learningRecordMapper)
                .selectList(any(LambdaQueryWrapper.class));

        List<Chapter> hotChapters = List.of(
                Chapter.builder().id(1L).title("标签A").build(),
                Chapter.builder().id(2L).title(null).build(),
                Chapter.builder().id(3L).title("标签C").build(),
                Chapter.builder().id(4L).title("标签D").build(),
                Chapter.builder().id(5L).title("标签E").build());
        doReturn(hotChapters).when(chapterMapper).selectList(any(LambdaQueryWrapper.class));

        doReturn(Collections.emptyList()).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));

        DynamicProfileResponse profile = userProfileService.buildDynamicProfile(userId);

        assertThat(profile.getHotTags()).hasSize(3);
        assertThat(profile.getHotTags().get(0).getTag()).isEqualTo("标签A");
        assertThat(profile.getHotTags().get(0).getCount()).isEqualTo(4L);
        assertThat(profile.getHotTags().get(1).getTag()).isEqualTo("标签C");
        assertThat(profile.getHotTags().get(2).getTag()).isEqualTo("标签D");
        assertThat(profile.getHotTags()).noneMatch(t -> "标签E".equals(t.getTag()));
        assertThat(profile.getHotTags()).noneMatch(t -> t.getTag() == null);
    }

    @Test
    @DisplayName("成长趋势：startTime为空回退createdAt、duration为空计0、时间全空与answerTime为空被排除")
    void buildDynamicProfileGrowthTrendCoversWeekBoundaryBranches() {
        String userId = "user-1";
        LocalDateTime now = LocalDateTime.now();

        UserLearningRecord r1 = record(1L, now, now, 100);
        UserLearningRecord r2 = record(1L, null, now, 200);
        UserLearningRecord r3 = record(1L, now, now, null);
        UserLearningRecord r4 = record(1L, null, null, 50);
        doReturn(Collections.emptyList(), List.of(r1, r2, r3, r4))
                .when(learningRecordMapper)
                .selectList(any(LambdaQueryWrapper.class));

        UserQuizAnswer a1 = UserQuizAnswer.builder()
                .userId(1L)
                .quizId(1L)
                .isCorrect(1)
                .answerTime(now)
                .build();
        UserQuizAnswer a2 = UserQuizAnswer.builder()
                .userId(1L)
                .quizId(2L)
                .isCorrect(1)
                .answerTime(null)
                .build();
        doReturn(List.of(a1, a2), Collections.emptyList(), Collections.emptyList())
                .when(quizAnswerMapper)
                .selectList(any(LambdaQueryWrapper.class));

        DynamicProfileResponse profile = userProfileService.buildDynamicProfile(userId);

        assertThat(profile.getGrowthTrend()).hasSize(8);
        assertThat(profile.getGrowthTrend().get(7).getStudyDuration()).isEqualTo(300);
        assertThat(profile.getGrowthTrend().get(7).getQuizAccuracy()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("薄弱域：答题记录quizId全为空时提前返回空列表")
    void buildDynamicProfileWeakDomainsWithNullQuizIdsReturnsEarly() {
        String userId = "user-1";

        doReturn(Collections.emptyList()).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));

        UserQuizAnswer a1 =
                UserQuizAnswer.builder().userId(1L).quizId(null).isCorrect(1).build();
        UserQuizAnswer a2 =
                UserQuizAnswer.builder().userId(1L).quizId(null).isCorrect(0).build();
        doReturn(Collections.emptyList(), List.of(a1, a2), List.of(a1, a2))
                .when(quizAnswerMapper)
                .selectList(any(LambdaQueryWrapper.class));

        DynamicProfileResponse profile = userProfileService.buildDynamicProfile(userId);

        assertThat(profile.getWeakDomains()).isEmpty();
        verify(quizMapper, never()).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("薄弱域：题目chapterId为null时跳过且无有效章节映射返回空列表")
    void buildDynamicProfileWeakDomainsWithNullChapterIdSkipsAndReturnsEmpty() {
        String userId = "user-1";

        doReturn(Collections.emptyList()).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));

        UserQuizAnswer a1 =
                UserQuizAnswer.builder().userId(1L).quizId(1L).isCorrect(1).build();
        UserQuizAnswer a2 =
                UserQuizAnswer.builder().userId(1L).quizId(1L).isCorrect(0).build();
        doReturn(Collections.emptyList(), List.of(a1, a2), List.of(a1, a2))
                .when(quizAnswerMapper)
                .selectList(any(LambdaQueryWrapper.class));

        Quiz quiz = Quiz.builder().id(1L).difficulty("easy").chapterId(null).build();
        doReturn(List.of(quiz)).when(quizMapper).selectList(any(LambdaQueryWrapper.class));

        DynamicProfileResponse profile = userProfileService.buildDynamicProfile(userId);

        assertThat(profile.getWeakDomains()).isEmpty();
        verify(chapterMapper, never()).selectList(any(LambdaQueryWrapper.class));
    }

    @Test
    @DisplayName("薄弱域：章节标题为null使用fallback名称且正确率达标章节不计入")
    void buildDynamicProfileWeakDomainsWithMissingChapterTitle() {
        String userId = "user-1";

        doReturn(Collections.emptyList()).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));

        UserQuizAnswer a1 =
                UserQuizAnswer.builder().userId(1L).quizId(1L).isCorrect(1).build();
        UserQuizAnswer a3 =
                UserQuizAnswer.builder().userId(1L).quizId(3L).isCorrect(0).build();
        UserQuizAnswer a4 =
                UserQuizAnswer.builder().userId(1L).quizId(3L).isCorrect(0).build();
        doReturn(Collections.emptyList(), List.of(a1, a3, a4), List.of(a1, a3, a4))
                .when(quizAnswerMapper)
                .selectList(any(LambdaQueryWrapper.class));

        Quiz quiz1 = Quiz.builder().id(1L).difficulty("easy").chapterId(10L).build();
        Quiz quiz3 = Quiz.builder().id(3L).difficulty("easy").chapterId(30L).build();
        doReturn(List.of(quiz1, quiz3)).when(quizMapper).selectList(any(LambdaQueryWrapper.class));

        Chapter strong = Chapter.builder().id(10L).title("强章节").build();
        Chapter weakNoTitle = Chapter.builder().id(30L).title(null).build();
        doReturn(List.of(strong, weakNoTitle)).when(chapterMapper).selectList(any(LambdaQueryWrapper.class));

        DynamicProfileResponse profile = userProfileService.buildDynamicProfile(userId);

        assertThat(profile.getWeakDomains())
                .anyMatch(w -> "CHAPTER".equals(w.getType()) && "章节30".equals(w.getName()) && w.getAccuracy() == 0.0);
        assertThat(profile.getWeakDomains()).noneMatch(w -> "CHAPTER".equals(w.getType()) && "强章节".equals(w.getName()));
    }

    // ==================== 当前用户入口 ====================

    @Test
    @DisplayName("getCurrentUserDynamicProfile 获取当前用户ID后委托 buildDynamicProfile")
    void getCurrentUserDynamicProfileDelegatesToBuildDynamicProfile() {
        doReturn("current-user").when(userService).getCurrentUserId();

        doReturn(Collections.emptyList()).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));

        DynamicProfileResponse profile = userProfileService.getCurrentUserDynamicProfile();

        assertThat(profile.getHotTags()).isEmpty();
        assertThat(profile.getGrowthTrend()).hasSize(8);
        assertThat(profile.getWeakDomains()).isEmpty();
        verify(userService).getCurrentUserId();
    }

    @Test
    @DisplayName("getCurrentUserLearningSummary 获取当前用户ID后委托 getLearningSummary")
    void getCurrentUserLearningSummaryDelegatesToGetLearningSummary() {
        doReturn("current-user").when(userService).getCurrentUserId();

        doReturn(Collections.emptyList()).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(chapterProgressMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(0L).when(chapterProgressMapper).selectCount(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));

        LearningSummaryResponse summary = userProfileService.getCurrentUserLearningSummary();

        assertThat(summary.getTheory().getTotalDuration()).isZero();
        assertThat(summary.getTheory().getCompletionRate()).isZero();
        assertThat(summary.getPolicyComprehension().getAvgCorrectRate()).isZero();
        assertThat(summary.getPolicyComprehension().getTotalAnswers()).isZero();
        verify(userService).getCurrentUserId();
    }

    private UserLearningRecord record(
            Long chapterId, LocalDateTime startTime, LocalDateTime createdAt, Integer duration) {
        return UserLearningRecord.builder()
                .userId(1L)
                .chapterId(chapterId)
                .startTime(startTime)
                .createdAt(createdAt)
                .duration(duration)
                .build();
    }

    // ==================== 互动表现维度（4.5） ====================

    @Test
    @DisplayName("getProfile 聚合互动维度：评论数/获赞数/点赞数/活跃周数")
    void getProfileAggregatesInteractionStats() {
        String userId = "user-1";
        doReturn(Collections.emptyList()).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(chapterProgressMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(0L).when(chapterProgressMapper).selectCount(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));

        Comment c1 = Comment.builder().id(1L).build();
        Comment c2 = Comment.builder().id(2L).build();
        // 第一次 selectList 取评论 ID 列表，第二次取 createdAt（活跃周数，本周 1 条）
        doReturn(
                        List.of(c1, c2),
                        List.of(Comment.builder().createdAt(LocalDateTime.now()).build()))
                .when(commentMapper)
                .selectList(any(LambdaQueryWrapper.class));
        doReturn(2L).when(commentMapper).selectCount(any(LambdaQueryWrapper.class));
        doReturn(3L).when(likeRecordMapper).selectCount(any(LambdaQueryWrapper.class));
        doReturn(List.of(LikeRecord.builder().createdAt(LocalDateTime.now()).build()))
                .when(likeRecordMapper)
                .selectList(any(LambdaQueryWrapper.class));

        UserProfileResponse profile = userProfileService.getProfile(userId);

        assertThat(profile.getInteraction()).isNotNull();
        assertThat(profile.getInteraction().getCommentCount()).isEqualTo(2L);
        assertThat(profile.getInteraction().getLikeGivenCount()).isEqualTo(3L);
        assertThat(profile.getInteraction().getActiveWeeks()).isEqualTo(1L);
        // 获赞数：他人对本人评论的点赞（commentIds=[1,2]，like 计数 3）
        assertThat(profile.getInteraction().getLikeReceivedCount()).isEqualTo(3L);
    }

    @Test
    @DisplayName("getLearningSummary 含互动表现维度（成长图谱三维）")
    void learningSummaryIncludesInteractionDimension() {
        String userId = "user-1";
        doReturn(Collections.emptyList()).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(chapterProgressMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(0L).when(chapterProgressMapper).selectCount(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(commentMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(5L).when(commentMapper).selectCount(any(LambdaQueryWrapper.class));
        doReturn(7L).when(likeRecordMapper).selectCount(any(LambdaQueryWrapper.class));

        LearningSummaryResponse summary = userProfileService.getLearningSummary(userId);

        assertThat(summary.getInteraction()).isNotNull();
        assertThat(summary.getInteraction().getCommentCount()).isEqualTo(5L);
        assertThat(summary.getInteraction().getLikeGivenCount()).isEqualTo(7L);
        assertThat(summary.getInteraction().getLikeReceivedCount()).isZero();
        assertThat(summary.getInteraction().getActiveWeeks()).isZero();
    }

    // ==================== 协同过滤画像强化（1.6-B） ====================

    @Test
    @DisplayName("buildDynamicProfile 融合相似用户热点：按相似度加权并入 hotTags")
    void dynamicProfileMergesCfHotTags() {
        String userId = "user-1";
        // 自有热点：党章学习 ×2
        UserLearningRecord own1 = UserLearningRecord.builder()
                .userId(1L)
                .chapterId(1L)
                .duration(100)
                .startTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
        UserLearningRecord own2 = UserLearningRecord.builder()
                .userId(1L)
                .chapterId(1L)
                .duration(100)
                .startTime(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();

        doReturn(Collections.emptyList()).when(quizAnswerMapper).selectList(any(LambdaQueryWrapper.class));

        Chapter c1 = Chapter.builder().id(1L).title("党章学习").build();
        Chapter c2 = Chapter.builder().id(2L).title("党史学习").build();
        doReturn(List.of(c1, c2)).when(chapterMapper).selectList(any(LambdaQueryWrapper.class));

        // 相似用户 1001（score 0.9）：党史学习 ×2 → 热度 9×2=18；相似用户 1002（score 0.5）：党史学习 ×1 → 5
        Page<UserSimilarity> similarityPage = new Page<>(1, 3);
        similarityPage.setRecords(List.of(
                UserSimilarity.builder()
                        .userId1(1L)
                        .userId2(1001L)
                        .similarityScore(new BigDecimal("0.9"))
                        .build(),
                UserSimilarity.builder()
                        .userId1(1L)
                        .userId2(1002L)
                        .similarityScore(new BigDecimal("0.5"))
                        .build()));
        doReturn(similarityPage).when(userSimilarityMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        // 相似用户的近 30 天学习记录。buildDynamicProfile 的调用顺序：
        // mergeHotTags(buildRecentHotTags → learningRecordMapper #1, buildCfHotTags → #2)
        // 之后 buildGrowthTrend → #3、buildWeakDomains（不查 learningRecordMapper）
        UserLearningRecord peer1a = UserLearningRecord.builder()
                .userId(1001L)
                .chapterId(2L)
                .createdAt(LocalDateTime.now())
                .build();
        UserLearningRecord peer1b = UserLearningRecord.builder()
                .userId(1001L)
                .chapterId(2L)
                .createdAt(LocalDateTime.now())
                .build();
        UserLearningRecord peer2 = UserLearningRecord.builder()
                .userId(1002L)
                .chapterId(2L)
                .createdAt(LocalDateTime.now())
                .build();
        doReturn(List.of(own1, own2), List.of(peer1a, peer1b, peer2), List.of(own1, own2))
                .when(learningRecordMapper)
                .selectList(any(LambdaQueryWrapper.class));

        DynamicProfileResponse profile = userProfileService.buildDynamicProfile(userId);

        // 自有「党章学习」2 + 协同过滤「党史学习」(9×2+5=23) 均进入 Top3
        assertThat(profile.getHotTags()).hasSize(2);
        assertThat(profile.getHotTags()).anyMatch(t -> "党章学习".equals(t.getTag()) && t.getCount() == 2L);
        assertThat(profile.getHotTags()).anyMatch(t -> "党史学习".equals(t.getTag()) && t.getCount() == 23L);
        // 加权热点排序：党史学习(23) 应在 党章学习(2) 之前
        assertThat(profile.getHotTags().get(0).getTag()).isEqualTo("党史学习");
    }
}
