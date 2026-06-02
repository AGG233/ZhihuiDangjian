package com.rauio.smartdangjian.server.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.neo4j.core.Neo4jClient;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.content.mapper.CategoryCourseMapper;
import com.rauio.smartdangjian.server.content.mapper.ChapterMapper;
import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryCourse;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.learning.mapper.UserChapterProgressMapper;
import com.rauio.smartdangjian.server.learning.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.server.learning.pojo.dto.UserBehaviorDto;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.search.pojo.response.UserProfileResponse;
import com.rauio.smartdangjian.server.search.support.TestMybatisPlusConfig;
import com.rauio.smartdangjian.server.user.mapper.UserSimilarityMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.UserSimilarity;
import com.rauio.smartdangjian.server.user.service.UserSimilarityService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RecommendService 推荐算法")
class RecommendServiceTest {

    @BeforeAll
    static void initMybatisPlus() {
        TestMybatisPlusConfig.ensureInitialized();
    }

    @Mock
    private UserLearningRecordMapper userLearningRecordMapper;

    @Mock
    private UserChapterProgressMapper userChapterProgressMapper;

    @Mock
    private UserSimilarityMapper userSimilarityMapper;

    @Mock
    private ChapterMapper chapterMapper;

    @Mock
    private CategoryCourseMapper categoryCourseMapper;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private UserSimilarityService userSimilarityService;

    @Mock
    private Neo4jClient neo4jClient;

    @Mock
    private UserProfileService userProfileService;

    private RecommendService recommendService;

    @BeforeEach
    void resetSpy() {
        recommendService = spy(new RecommendService(
                userLearningRecordMapper,
                userChapterProgressMapper,
                userSimilarityMapper,
                chapterMapper,
                categoryCourseMapper,
                courseMapper,
                userSimilarityService,
                neo4jClient,
                userProfileService,
                Clock.fixed(Instant.parse("2026-05-31T10:15:30Z"), ZoneId.of("UTC"))));
    }

    @Mock
    private Neo4jClient.UnboundRunnableSpec unboundRunnableSpec;

    private Neo4jClient.RunnableSpec runnableSpec;
    private Neo4jClient.RecordFetchSpec<String> recordFetchSpec;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUpNeo4jClient() {
        runnableSpec = mock(Neo4jClient.RunnableSpec.class);
        Neo4jClient.OngoingBindSpec ongoingBindSpec = mock(Neo4jClient.OngoingBindSpec.class);
        Neo4jClient.MappingSpec mappingSpec = mock(Neo4jClient.MappingSpec.class);
        recordFetchSpec = mock(Neo4jClient.RecordFetchSpec.class);

        when(neo4jClient.query(anyString())).thenReturn(unboundRunnableSpec);
        when(unboundRunnableSpec.bind(any())).thenReturn(ongoingBindSpec);
        when(ongoingBindSpec.to(anyString())).thenReturn(runnableSpec);
        when(runnableSpec.fetchAs(String.class)).thenReturn(mappingSpec);
        when(mappingSpec.mappedBy(any())).thenReturn(recordFetchSpec);
        when(recordFetchSpec.all()).thenReturn(Collections.emptyList());
    }

    // ==================== recommend ====================

    @Test
    @DisplayName("综合推荐合并多个来源的结果并分页")
    void recommendMergesMultipleSources() {
        Page<Long> cfPage = new Page<>(1, 10);
        cfPage.setRecords(List.of(1L, 2L));
        Page<Long> graphPage = new Page<>(1, 10);
        graphPage.setRecords(List.of(2L, 3L));
        Page<Long> profilePage = new Page<>(1, 10);
        profilePage.setRecords(List.of(3L));

        doReturn(cfPage).when(recommendService).recommendByCF(1L, 1, 10);
        doReturn(graphPage).when(recommendService).recommendByGraph(1L, 1, 10);
        doReturn(profilePage).when(recommendService).recommendByProfile(1L, 1, 10);

        Page<Long> result = recommendService.recommend(1L, 1, 10);

        assertThat(result.getRecords()).isNotEmpty();
        assertThat(result.getRecords().get(0)).isEqualTo(2L);
    }

    @Test
    @DisplayName("综合推荐所有来源均为空时返回空页")
    void recommendAllEmptyReturnsEmptyPage() {
        Page<Long> emptyPage = new Page<>(1, 10);
        doReturn(emptyPage).when(recommendService).recommendByCF(1L, 1, 10);
        doReturn(emptyPage).when(recommendService).recommendByGraph(1L, 1, 10);
        doReturn(emptyPage).when(recommendService).recommendByProfile(1L, 1, 10);

        Page<Long> result = recommendService.recommend(1L, 1, 10);

        assertThat(result.getRecords()).isEmpty();
    }

    // ==================== recommendByCF ====================

    @Test
    @DisplayName("协同过滤无相似用户时返回空页")
    void recommendByCFNoSimilarUsersReturnsEmptyPage() {
        Page<UserSimilarity> similarityPage = new Page<>(1, 10);
        similarityPage.setRecords(Collections.emptyList());
        doReturn(similarityPage).when(userSimilarityMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        Page<Long> result = recommendService.recommendByCF(1L, 1, 10);

        assertThat(result.getRecords()).isEmpty();
    }

    @Test
    @DisplayName("协同过滤有相似用户时按学习章节推荐课程")
    void recommendByCFWithSimilarUsersReturnsRecommendedCourses() {
        UserSimilarity sim = UserSimilarity.builder().userId1(1L).userId2(2L).build();
        Page<UserSimilarity> similarityPage = new Page<>(1, 10, 1);
        similarityPage.setRecords(List.of(sim));
        doReturn(similarityPage).when(userSimilarityMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        UserLearningRecord learned =
                UserLearningRecord.builder().chapterId(1L).userId(1L).build();
        doReturn(List.of(learned)).when(userLearningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        Chapter learnedChapter = Chapter.builder().id(1L).courseId(1L).build();
        doReturn(List.of(learnedChapter)).when(chapterMapper).selectList(any(LambdaQueryWrapper.class));

        UserLearningRecord similarRecord =
                UserLearningRecord.builder().chapterId(2L).userId(2L).build();
        doReturn(List.of(learned), List.of(similarRecord))
                .when(userLearningRecordMapper)
                .selectList(any(LambdaQueryWrapper.class));

        doReturn(Collections.emptyList()).when(userChapterProgressMapper).selectList(any(LambdaQueryWrapper.class));

        Chapter chapter = Chapter.builder().id(2L).courseId(2L).title("章节2").build();
        doReturn(List.of(chapter)).when(chapterMapper).selectByIds(anyCollection());

        Page<Long> result = recommendService.recommendByCF(1L, 1, 10);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0)).isEqualTo(2L);
    }

    @Test
    @DisplayName("协同过滤用户已学完全部相似课程时返回空页")
    void recommendByCFUserAlreadyLearnedAllReturnsEmptyPage() {
        UserSimilarity sim = UserSimilarity.builder().userId1(1L).userId2(2L).build();
        Page<UserSimilarity> similarityPage = new Page<>(1, 10, 1);
        similarityPage.setRecords(List.of(sim));
        doReturn(similarityPage).when(userSimilarityMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        UserLearningRecord learned =
                UserLearningRecord.builder().chapterId(1L).userId(1L).build();
        doReturn(List.of(learned)).when(userLearningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        Chapter learnedChapter = Chapter.builder().id(1L).courseId(1L).build();
        doReturn(List.of(learnedChapter)).when(chapterMapper).selectList(any(LambdaQueryWrapper.class));

        UserLearningRecord similarRecord =
                UserLearningRecord.builder().chapterId(1L).userId(1L).build();
        doReturn(List.of(learned), List.of(similarRecord))
                .when(userLearningRecordMapper)
                .selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(userChapterProgressMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(List.of(learnedChapter)).when(chapterMapper).selectByIds(anyCollection());

        Page<Long> result = recommendService.recommendByCF(1L, 1, 10);

        assertThat(result.getRecords()).isEmpty();
    }

    // ==================== recommendByGraph ====================

    @Test
    @DisplayName("知识图谱推荐返回课程ID列表")
    void recommendByGraphReturnsCourseIds() {
        when(recordFetchSpec.all()).thenReturn(List.of("1", "2"));

        Page<Long> result = recommendService.recommendByGraph(1L, 1, 10);

        assertThat(result.getRecords()).containsExactly(1L, 2L);
    }

    @Test
    @DisplayName("知识图谱无结果时返回空页")
    void recommendByGraphEmptyReturnsEmptyPage() {
        Page<Long> result = recommendService.recommendByGraph(1L, 1, 10);

        assertThat(result.getRecords()).isEmpty();
    }

    // ==================== recommendByProfile ====================

    @Test
    @DisplayName("用户画像为空时返回空页")
    void recommendByProfileNullProfileReturnsEmptyPage() {
        doReturn(null).when(userProfileService).getProfile("1");

        Page<Long> result = recommendService.recommendByProfile(1L, 1, 10);

        assertThat(result.getRecords()).isEmpty();
    }

    @Test
    @DisplayName("根据兴趣分类推荐课程")
    void recommendByProfileWithInterestsReturnsCourseIds() {
        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("1")
                .interestCategoryIds(List.of(1L))
                .build();
        doReturn(profile).when(userProfileService).getProfile("1");

        doReturn(Collections.emptyList()).when(userLearningRecordMapper).selectList(any(LambdaQueryWrapper.class));

        CategoryCourse cc = CategoryCourse.builder().courseId(1L).categoryId(1L).build();
        doReturn(List.of(cc)).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));

        Course course = Course.builder().id(1L).title("兴趣课程").build();
        Page<Course> coursePage = new Page<>(1, 10, 1);
        coursePage.setRecords(List.of(course));
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        Page<Long> result = recommendService.recommendByProfile(1L, 1, 10);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0)).isEqualTo(1L);
    }

    @Test
    @DisplayName("兴趣分类无匹配课程时返回空页")
    void recommendByProfileNoInterestMatchReturnsEmptyPage() {
        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("1")
                .interestCategoryIds(List.of(999L))
                .build();
        doReturn(profile).when(userProfileService).getProfile("1");
        doReturn(Collections.emptyList()).when(userLearningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));

        Page<Long> result = recommendService.recommendByProfile(1L, 1, 10);

        assertThat(result.getRecords()).isEmpty();
    }

    @Test
    @DisplayName("答题正确率高于80%时推荐hard难度课程")
    void recommendByProfileWithHighCorrectRateFiltersHard() {
        UserProfileResponse.QuizStats quizStats =
                UserProfileResponse.QuizStats.builder().correctRate(0.85).build();
        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("1")
                .interestCategoryIds(List.of(1L))
                .quiz(quizStats)
                .build();
        doReturn(profile).when(userProfileService).getProfile("1");
        doReturn(Collections.emptyList()).when(userLearningRecordMapper).selectList(any(LambdaQueryWrapper.class));

        CategoryCourse cc = CategoryCourse.builder().courseId(1L).categoryId(1L).build();
        doReturn(List.of(cc)).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));

        Course course = Course.builder().id(1L).difficulty("advanced").build();
        Page<Course> coursePage = new Page<>(1, 10, 1);
        coursePage.setRecords(List.of(course));
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        Page<Long> result = recommendService.recommendByProfile(1L, 1, 10);

        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    @DisplayName("答题正确率在50%-80%时推荐medium难度课程")
    void recommendByProfileWithMediumCorrectRateFiltersMedium() {
        UserProfileResponse.QuizStats quizStats =
                UserProfileResponse.QuizStats.builder().correctRate(0.65).build();
        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("1")
                .interestCategoryIds(List.of(1L))
                .quiz(quizStats)
                .build();
        doReturn(profile).when(userProfileService).getProfile("1");
        doReturn(Collections.emptyList()).when(userLearningRecordMapper).selectList(any(LambdaQueryWrapper.class));

        CategoryCourse cc = CategoryCourse.builder().courseId(1L).categoryId(1L).build();
        doReturn(List.of(cc)).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));

        Course course = Course.builder().id(1L).difficulty("intermediate").build();
        Page<Course> coursePage = new Page<>(1, 10, 1);
        coursePage.setRecords(List.of(course));
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        Page<Long> result = recommendService.recommendByProfile(1L, 1, 10);

        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    @DisplayName("答题正确率低于50%时推荐easy难度课程")
    void recommendByProfileWithLowCorrectRateFiltersEasy() {
        UserProfileResponse.QuizStats quizStats =
                UserProfileResponse.QuizStats.builder().correctRate(0.35).build();
        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("1")
                .interestCategoryIds(List.of(1L))
                .quiz(quizStats)
                .build();
        doReturn(profile).when(userProfileService).getProfile("1");
        doReturn(Collections.emptyList()).when(userLearningRecordMapper).selectList(any(LambdaQueryWrapper.class));

        CategoryCourse cc = CategoryCourse.builder().courseId(1L).categoryId(1L).build();
        doReturn(List.of(cc)).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));

        Course course = Course.builder().id(1L).difficulty("beginner").build();
        Page<Course> coursePage = new Page<>(1, 10, 1);
        coursePage.setRecords(List.of(course));
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        Page<Long> result = recommendService.recommendByProfile(1L, 1, 10);

        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    @DisplayName("用户画像无兴趣分类时不过滤分类")
    void recommendByProfileNoInterestsSkipsCategoryFilter() {
        UserProfileResponse profile = UserProfileResponse.builder().userId("1").build();
        doReturn(profile).when(userProfileService).getProfile("1");
        doReturn(Collections.emptyList()).when(userLearningRecordMapper).selectList(any(LambdaQueryWrapper.class));

        Course course = Course.builder().id(1L).build();
        Page<Course> coursePage = new Page<>(1, 10, 1);
        coursePage.setRecords(List.of(course));
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        Page<Long> result = recommendService.recommendByProfile(1L, 1, 10);

        assertThat(result.getRecords()).hasSize(1);
    }

    // ==================== paginate ====================

    @Test
    @DisplayName("分页超过数据范围时返回空列表")
    void paginateBeyondRangeReturnsEmpty() {
        Page<Long> cfPage = new Page<>(1, 10);
        cfPage.setRecords(List.of(1L));
        Page<Long> graphPage = new Page<>(1, 10);
        Page<Long> profilePage = new Page<>(1, 10);

        doReturn(cfPage).when(recommendService).recommendByCF(anyLong(), anyInt(), anyInt());
        doReturn(graphPage).when(recommendService).recommendByGraph(anyLong(), anyInt(), anyInt());
        doReturn(profilePage).when(recommendService).recommendByProfile(anyLong(), anyInt(), anyInt());

        Page<Long> result = recommendService.recommend(1L, 10, 10);

        assertThat(result.getRecords()).isEmpty();
    }

    // ==================== calculateSimilarity ====================

    @Test
    @DisplayName("定时计算相似度：无行为数据时直接返回")
    void calculateSimilarityEmptyBehaviors() {
        doReturn(Collections.emptyList()).when(userLearningRecordMapper).getAllUserBehaviors();

        recommendService.calculateSimilarity();

        verify(userSimilarityMapper, never()).delete(any());
    }

    @Test
    @DisplayName("定时计算相似度：正常流程创建相似度记录")
    void calculateSimilarityNormalFlow() {
        UserBehaviorDto b1 = new UserBehaviorDto();
        b1.setUserId(1L);
        b1.setChapterId(10L);
        UserBehaviorDto b2 = new UserBehaviorDto();
        b2.setUserId(2L);
        b2.setChapterId(10L);

        doReturn(List.of(b1, b2)).when(userLearningRecordMapper).getAllUserBehaviors();

        recommendService.calculateSimilarity();

        verify(userSimilarityService).saveBatch(anyList());
    }

    @Test
    @DisplayName("定时计算相似度：低相似度（< 0.1）被过滤，其他相似度保留")
    void calculateSimilarityLowScoreFiltered() {
        // User 1: 101 chapters (vector length ~10.05)
        // User 2: chapter {1} (vector length 1)
        // User 3: chapter {1} (vector length 1)
        // All share chapter 1.
        // Score(1,2) = 1/(10.05*1) = 0.0995 < 0.1 -> filtered
        // Score(1,3) = 0.0995 -> filtered
        // Score(2,3) = 1/(1*1) = 1.0 -> kept
        List<UserBehaviorDto> behaviors = new java.util.ArrayList<>();
        for (long i = 1; i <= 101; i++) {
            UserBehaviorDto ub = new UserBehaviorDto();
            ub.setUserId(1L);
            ub.setChapterId(i);
            behaviors.add(ub);
        }
        UserBehaviorDto u2 = new UserBehaviorDto();
        u2.setUserId(2L);
        u2.setChapterId(1L);
        behaviors.add(u2);
        UserBehaviorDto u3 = new UserBehaviorDto();
        u3.setUserId(3L);
        u3.setChapterId(1L);
        behaviors.add(u3);

        doReturn(behaviors).when(userLearningRecordMapper).getAllUserBehaviors();

        recommendService.calculateSimilarity();

        verify(userSimilarityService).saveBatch(anyList());
    }

    @Test
    @DisplayName("定时计算相似度：用户学习章节数少于2时跳过共现")
    void calculateSimilaritySingleUserPerChapterSkipped() {
        // Only 1 user studies chapter 1 -> userList size < 2 -> skipped
        UserBehaviorDto b1 = new UserBehaviorDto();
        b1.setUserId(1L);
        b1.setChapterId(1L);

        doReturn(List.of(b1)).when(userLearningRecordMapper).getAllUserBehaviors();

        recommendService.calculateSimilarity();

        verify(userSimilarityMapper).delete(any());
        // No buffer -> saveBatch not called
    }

    // ==================== recommendByCF - allInvolvedChapterIds empty ====================

    @Test
    @DisplayName("协同过滤相似用户的章节ID全部为空时返回空页")
    void recommendByCFAllChapterIdsEmpty() {
        UserSimilarity sim = UserSimilarity.builder().userId1(1L).userId2(2L).build();
        Page<UserSimilarity> similarityPage = new Page<>(1, 10, 1);
        similarityPage.setRecords(List.of(sim));
        doReturn(similarityPage).when(userSimilarityMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        // user's own learned records have chapterIds
        UserLearningRecord learned =
                UserLearningRecord.builder().chapterId(1L).userId(1L).build();
        // similar user's records have null chapterIds
        UserLearningRecord nullChapter =
                UserLearningRecord.builder().chapterId(null).userId(2L).build();
        doReturn(List.of(learned), List.of(nullChapter))
                .when(userLearningRecordMapper)
                .selectList(any(LambdaQueryWrapper.class));
        Chapter learnedChapter = Chapter.builder().id(1L).courseId(1L).build();
        doReturn(List.of(learnedChapter)).when(chapterMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(userChapterProgressMapper).selectList(any(LambdaQueryWrapper.class));

        Page<Long> result = recommendService.recommendByCF(1L, 1, 10);

        assertThat(result.getRecords()).isEmpty();
    }

    // ==================== recommendByProfile - edge cases ====================

    @Test
    @DisplayName("画像推荐：兴趣分类列表为空时不过滤分类")
    void recommendByProfileEmptyInterests() {
        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("1")
                .interestCategoryIds(Collections.emptyList())
                .build();
        doReturn(profile).when(userProfileService).getProfile("1");
        doReturn(Collections.emptyList()).when(userLearningRecordMapper).selectList(any(LambdaQueryWrapper.class));

        Course course = Course.builder().id(1L).build();
        Page<Course> coursePage = new Page<>(1, 10, 1);
        coursePage.setRecords(List.of(course));
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        Page<Long> result = recommendService.recommendByProfile(1L, 1, 10);

        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    @DisplayName("画像推荐：答题正确率为0时不过滤难度")
    void recommendByProfileQuizZeroCorrectRate() {
        UserProfileResponse.QuizStats quizStats =
                UserProfileResponse.QuizStats.builder().correctRate(0.0).build();
        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("1")
                .interestCategoryIds(List.of(1L))
                .quiz(quizStats)
                .build();
        doReturn(profile).when(userProfileService).getProfile("1");
        doReturn(Collections.emptyList()).when(userLearningRecordMapper).selectList(any(LambdaQueryWrapper.class));

        CategoryCourse cc = CategoryCourse.builder().courseId(1L).categoryId(1L).build();
        doReturn(List.of(cc)).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));

        Course course = Course.builder().id(1L).build();
        Page<Course> coursePage = new Page<>(1, 10, 1);
        coursePage.setRecords(List.of(course));
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        Page<Long> result = recommendService.recommendByProfile(1L, 1, 10);

        assertThat(result.getRecords()).hasSize(1);
    }
}
