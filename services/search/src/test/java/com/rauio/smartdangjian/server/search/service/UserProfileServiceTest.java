package com.rauio.smartdangjian.server.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
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
import com.rauio.smartdangjian.server.search.pojo.response.UserProfileResponse;
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
                .userId(1L).chapterId(1L).duration(600).deviceType("web").build();
        UserLearningRecord record2 = UserLearningRecord.builder()
                .userId(1L).chapterId(1L).duration(300).deviceType("web").build();
        doReturn(List.of(record1, record2)).when(learningRecordMapper).selectList(any(LambdaQueryWrapper.class));

        UserChapterProgress progress = UserChapterProgress.builder()
                .userId(1L).chapterId(1L).progress(100).status("completed").build();
        doReturn(List.of(progress)).when(chapterProgressMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(1L).when(chapterProgressMapper).selectCount(any(LambdaQueryWrapper.class));

        Chapter chapter = Chapter.builder().id(1L).courseId(1L).build();
        Chapter chapter2 = Chapter.builder().id(2L).courseId(1L).build();
        doReturn(List.of(chapter, chapter2)).when(chapterMapper).selectList(any(LambdaQueryWrapper.class));

        CategoryCourse cc = CategoryCourse.builder().courseId(1L).categoryId(1L).build();
        CategoryCourse cc2 = CategoryCourse.builder().courseId(1L).categoryId(1L).build();
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
                .userId(1L).quizId(1L).isCorrect(1).timeSpent(30).build();
        UserQuizAnswer a2 = UserQuizAnswer.builder()
                .userId(1L).quizId(2L).isCorrect(0).timeSpent(60).build();
        UserQuizAnswer a3 = UserQuizAnswer.builder()
                .userId(1L).quizId(3L).isCorrect(1).timeSpent(45).build();
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
                .userId(1L).chapterId(1L).duration(null).deviceType("mobile").build();
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
                .userId(1L).chapterId(1L).progress(null).status("in_progress").build();
        UserChapterProgress p2 = UserChapterProgress.builder()
                .userId(1L).chapterId(2L).progress(60).status("in_progress").build();
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
}
