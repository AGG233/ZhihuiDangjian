package com.rauio.smartdangjian.crosslayer.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
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
import com.rauio.smartdangjian.server.search.service.UserProfileService;
import com.rauio.smartdangjian.server.user.service.UserService;

/**
 * 用户画像展示跨层回归测试。
 *
 * <p>装配真实 {@link UserProfileService}（构造器直查 mapper），Mapper 以
 * {@link MockitoBean} 提供（Spring 在用例之间自动重置）。造学习记录 + 进度 +
 * 答题记录，断言 learning-summary 与 dynamic 画像的聚合结果正确。
 */
@SpringBootTest(classes = ProfileSummaryFlowTest.TestConfig.class)
@TestPropertySource(properties = {"spring.ai.model.embedding=dashscope", "spring.ai.vectorstore.type=none"})
@DisplayName("用户画像展示跨层回归")
class ProfileSummaryFlowTest extends CrossLayerTestBase {

    @MockitoBean
    private UserLearningRecordMapper learningRecordMapper;

    @MockitoBean
    private UserChapterProgressMapper chapterProgressMapper;

    @MockitoBean
    private UserQuizAnswerMapper quizAnswerMapper;

    @MockitoBean
    private QuizMapper quizMapper;

    @MockitoBean
    private ChapterMapper chapterMapper;

    @MockitoBean
    private CategoryCourseMapper categoryCourseMapper;

    @MockitoBean
    private UserService userService;

    @Autowired
    private UserProfileService userProfileService;

    @BeforeAll
    static void initMybatisPlus() {
        MybatisConfiguration config = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(config, "");
        TableInfoHelper.initTableInfo(assistant, UserLearningRecord.class);
        TableInfoHelper.initTableInfo(assistant, UserChapterProgress.class);
        TableInfoHelper.initTableInfo(assistant, UserQuizAnswer.class);
        TableInfoHelper.initTableInfo(assistant, Quiz.class);
        TableInfoHelper.initTableInfo(assistant, Chapter.class);
        TableInfoHelper.initTableInfo(assistant, CategoryCourse.class);
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        UserProfileService userProfileService(
                UserLearningRecordMapper learningRecordMapper,
                UserChapterProgressMapper chapterProgressMapper,
                UserQuizAnswerMapper quizAnswerMapper,
                QuizMapper quizMapper,
                ChapterMapper chapterMapper,
                CategoryCourseMapper categoryCourseMapper,
                UserService userService) {
            return new UserProfileService(
                    learningRecordMapper,
                    chapterProgressMapper,
                    quizAnswerMapper,
                    quizMapper,
                    chapterMapper,
                    categoryCourseMapper,
                    userService);
        }
    }

    @Test
    @DisplayName("学习记录+进度+答题：learning-summary 理论=时长/完成率，政策=平均正确率")
    void learningSummaryAggregatesTheoryAndPolicyDimensions() {
        UserLearningRecord r1 = UserLearningRecord.builder()
                .userId(1L)
                .chapterId(1L)
                .duration(600)
                .build();
        UserLearningRecord r2 = UserLearningRecord.builder()
                .userId(1L)
                .chapterId(2L)
                .duration(300)
                .build();
        when(learningRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(r1, r2));

        when(chapterProgressMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(1L);
        UserChapterProgress p1 = UserChapterProgress.builder()
                .userId(1L)
                .chapterId(1L)
                .progress(100)
                .status("completed")
                .build();
        UserChapterProgress p2 = UserChapterProgress.builder()
                .userId(1L)
                .chapterId(2L)
                .progress(60)
                .status("in_progress")
                .build();
        when(chapterProgressMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(p1, p2));

        UserQuizAnswer a1 =
                UserQuizAnswer.builder().userId(1L).quizId(1L).isCorrect(1).build();
        UserQuizAnswer a2 =
                UserQuizAnswer.builder().userId(1L).quizId(2L).isCorrect(0).build();
        UserQuizAnswer a3 =
                UserQuizAnswer.builder().userId(1L).quizId(3L).isCorrect(1).build();
        when(quizAnswerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(a1, a2, a3));

        Quiz q1 = Quiz.builder().id(1L).difficulty("easy").build();
        Quiz q2 = Quiz.builder().id(2L).difficulty("easy").build();
        Quiz q3 = Quiz.builder().id(3L).difficulty("medium").build();
        when(quizMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(q1, q2, q3));

        LearningSummaryResponse summary = userProfileService.getLearningSummary("1");

        assertThat(summary.getTheory().getTotalDuration()).isEqualTo(900);
        assertThat(summary.getTheory().getCompletionRate()).isEqualTo(0.5);
        assertThat(summary.getPolicyComprehension().getAvgCorrectRate()).isEqualTo(2.0 / 3.0);
        assertThat(summary.getPolicyComprehension().getTotalAnswers()).isEqualTo(3);
    }

    @Test
    @DisplayName("无数据：learning-summary 返回零值不报错")
    void learningSummaryEmptyDataReturnsZeros() {
        when(learningRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(chapterProgressMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(chapterProgressMapper.selectCount(any(LambdaQueryWrapper.class))).thenReturn(0L);
        when(quizAnswerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        LearningSummaryResponse summary = userProfileService.getLearningSummary("1");

        assertThat(summary.getTheory().getTotalDuration()).isZero();
        assertThat(summary.getTheory().getCompletionRate()).isZero();
        assertThat(summary.getPolicyComprehension().getAvgCorrectRate()).isZero();
        assertThat(summary.getPolicyComprehension().getTotalAnswers()).isZero();
    }

    @Test
    @DisplayName("学习+答题聚合：动态画像返回热点标签Top3、8周趋势与薄弱知识域")
    void dynamicProfileAggregatesHotTagsTrendAndWeakDomains() {
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
        when(learningRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(r1, r2, r3, r4));

        Chapter c1 = Chapter.builder().id(1L).title("党章学习").build();
        Chapter c2 = Chapter.builder().id(2L).title("党史学习").build();
        Chapter c3 = Chapter.builder().id(3L).title("组织建设").build();
        Chapter c20 = Chapter.builder().id(20L).title("薄弱章节").build();
        when(chapterMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(c1, c2, c3, c20));

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
        when(quizAnswerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(a1, a2, a3));

        Quiz q1 = Quiz.builder().id(1L).difficulty("easy").chapterId(10L).build();
        Quiz q2 = Quiz.builder().id(2L).difficulty("hard").chapterId(20L).build();
        when(quizMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of(q1, q2));

        DynamicProfileResponse profile = userProfileService.buildDynamicProfile("1");

        assertThat(profile.getHotTags()).hasSize(3);
        assertThat(profile.getHotTags().get(0).getTag()).isEqualTo("党章学习");
        assertThat(profile.getHotTags().get(0).getCount()).isEqualTo(2L);

        assertThat(profile.getGrowthTrend()).hasSize(8);
        assertThat(profile.getGrowthTrend().get(7).getStudyDuration()).isEqualTo(400);
        assertThat(profile.getGrowthTrend().get(7).getQuizAccuracy()).isEqualTo(1.0 / 3.0);

        assertThat(profile.getWeakDomains())
                .anyMatch(w -> "DIFFICULTY".equals(w.getType()) && "hard".equals(w.getName()));
        assertThat(profile.getWeakDomains()).anyMatch(w -> "CHAPTER".equals(w.getType()) && "薄弱章节".equals(w.getName()));
    }

    @Test
    @DisplayName("无数据：动态画像返回空热点/零值趋势/空薄弱域")
    void dynamicProfileNoDataReturnsEmptyStructures() {
        when(learningRecordMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        when(quizAnswerMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());

        DynamicProfileResponse profile = userProfileService.buildDynamicProfile("1");

        assertThat(profile.getHotTags()).isEmpty();
        assertThat(profile.getGrowthTrend()).hasSize(8);
        assertThat(profile.getGrowthTrend().get(7).getStudyDuration()).isZero();
        assertThat(profile.getGrowthTrend().get(7).getQuizAccuracy()).isZero();
        assertThat(profile.getWeakDomains()).isEmpty();
    }
}
