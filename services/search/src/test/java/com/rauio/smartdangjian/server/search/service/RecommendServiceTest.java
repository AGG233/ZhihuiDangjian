package com.rauio.smartdangjian.server.search.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.neo4j.core.Neo4jClient;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.server.content.mapper.CategoryCourseMapper;
import com.rauio.smartdangjian.server.content.mapper.ChapterMapper;
import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryCourse;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.content.pojo.entity.Course;
import com.rauio.smartdangjian.server.learning.mapper.UserChapterProgressMapper;
import com.rauio.smartdangjian.server.learning.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserChapterProgress;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.search.pojo.response.UserProfileResponse;
import com.rauio.smartdangjian.server.user.mapper.UserSimilarityMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.UserSimilarity;
import com.rauio.smartdangjian.server.user.service.UserSimilarityService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RecommendService 推荐算法")
class RecommendServiceTest {

    @BeforeAll
    static void initMybatisPlus() {
        MybatisConfiguration config = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(config, "");
        TableInfoHelper.initTableInfo(assistant, Course.class);
        TableInfoHelper.initTableInfo(assistant, CategoryCourse.class);
        TableInfoHelper.initTableInfo(assistant, Chapter.class);
        TableInfoHelper.initTableInfo(assistant, UserSimilarity.class);
        TableInfoHelper.initTableInfo(assistant, UserLearningRecord.class);
        TableInfoHelper.initTableInfo(assistant, UserChapterProgress.class);
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

    @Spy
    @InjectMocks
    private RecommendService recommendService;

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
        Page<String> cfPage = new Page<>(1, 10);
        cfPage.setRecords(List.of("c-1", "c-2"));
        Page<String> graphPage = new Page<>(1, 10);
        graphPage.setRecords(List.of("c-2", "c-3"));
        Page<String> profilePage = new Page<>(1, 10);
        profilePage.setRecords(List.of("c-3"));

        doReturn(cfPage).when(recommendService).recommendByCF("user-1", 1, 10);
        doReturn(graphPage).when(recommendService).recommendByGraph("user-1", 1, 10);
        doReturn(profilePage).when(recommendService).recommendByProfile("user-1", 1, 10);

        Page<String> result = recommendService.recommend("user-1", 1, 10);

        assertThat(result.getRecords()).isNotEmpty();
        assertThat(result.getRecords().get(0)).isEqualTo("c-2");
    }

    @Test
    @DisplayName("综合推荐所有来源均为空时返回空页")
    void recommendAllEmptyReturnsEmptyPage() {
        Page<String> emptyPage = new Page<>(1, 10);
        doReturn(emptyPage).when(recommendService).recommendByCF("user-1", 1, 10);
        doReturn(emptyPage).when(recommendService).recommendByGraph("user-1", 1, 10);
        doReturn(emptyPage).when(recommendService).recommendByProfile("user-1", 1, 10);

        Page<String> result = recommendService.recommend("user-1", 1, 10);

        assertThat(result.getRecords()).isEmpty();
    }

    // ==================== recommendByCF ====================

    @Test
    @DisplayName("协同过滤无相似用户时返回空页")
    void recommendByCFNoSimilarUsersReturnsEmptyPage() {
        Page<UserSimilarity> similarityPage = new Page<>(1, 10);
        similarityPage.setRecords(Collections.emptyList());
        doReturn(similarityPage).when(userSimilarityMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        Page<String> result = recommendService.recommendByCF("user-1", 1, 10);

        assertThat(result.getRecords()).isEmpty();
    }

    @Test
    @DisplayName("协同过滤有相似用户时按学习章节推荐课程")
    void recommendByCFWithSimilarUsersReturnsRecommendedCourses() {
        UserSimilarity sim = UserSimilarity.builder().userId1("user-1").userId2("user-2").build();
        Page<UserSimilarity> similarityPage = new Page<>(1, 10, 1);
        similarityPage.setRecords(List.of(sim));
        doReturn(similarityPage).when(userSimilarityMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        UserLearningRecord learned = UserLearningRecord.builder().chapterId("ch-learned").userId("user-1").build();
        doReturn(List.of(learned)).when(userLearningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        Chapter learnedChapter = Chapter.builder().id("ch-learned").courseId("c-learned").build();
        doReturn(List.of(learnedChapter)).when(chapterMapper).selectList(any(LambdaQueryWrapper.class));

        UserLearningRecord similarRecord = UserLearningRecord.builder().chapterId("ch-1").userId("user-2").build();
        doReturn(List.of(learned), List.of(similarRecord)).when(userLearningRecordMapper).selectList(any(LambdaQueryWrapper.class));

        doReturn(Collections.emptyList()).when(userChapterProgressMapper).selectList(any(LambdaQueryWrapper.class));

        Chapter chapter = Chapter.builder().id("ch-1").courseId("c-1").title("章节1").build();
        doReturn(List.of(chapter)).when(chapterMapper).selectByIds(anyCollection());

        Page<String> result = recommendService.recommendByCF("user-1", 1, 10);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0)).isEqualTo("c-1");
    }

    @Test
    @DisplayName("协同过滤用户已学完全部相似课程时返回空页")
    void recommendByCFUserAlreadyLearnedAllReturnsEmptyPage() {
        UserSimilarity sim = UserSimilarity.builder().userId1("user-1").userId2("user-2").build();
        Page<UserSimilarity> similarityPage = new Page<>(1, 10, 1);
        similarityPage.setRecords(List.of(sim));
        doReturn(similarityPage).when(userSimilarityMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        UserLearningRecord learned = UserLearningRecord.builder().chapterId("ch-1").userId("user-1").build();
        doReturn(List.of(learned)).when(userLearningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        Chapter learnedChapter = Chapter.builder().id("ch-1").courseId("c-1").build();
        doReturn(List.of(learnedChapter)).when(chapterMapper).selectList(any(LambdaQueryWrapper.class));

        UserLearningRecord similarRecord = UserLearningRecord.builder().chapterId("ch-1").userId("user-2").build();
        doReturn(List.of(learned), List.of(similarRecord)).when(userLearningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(userChapterProgressMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(List.of(learnedChapter)).when(chapterMapper).selectByIds(anyCollection());

        Page<String> result = recommendService.recommendByCF("user-1", 1, 10);

        assertThat(result.getRecords()).isEmpty();
    }

    // ==================== recommendByGraph ====================

    @Test
    @DisplayName("知识图谱推荐返回课程ID列表")
    void recommendByGraphReturnsCourseIds() {
        when(recordFetchSpec.all()).thenReturn(List.of("c-graph-1", "c-graph-2"));

        Page<String> result = recommendService.recommendByGraph("user-1", 1, 10);

        assertThat(result.getRecords()).containsExactly("c-graph-1", "c-graph-2");
    }

    @Test
    @DisplayName("知识图谱无结果时返回空页")
    void recommendByGraphEmptyReturnsEmptyPage() {
        Page<String> result = recommendService.recommendByGraph("user-1", 1, 10);

        assertThat(result.getRecords()).isEmpty();
    }

    // ==================== recommendByProfile ====================

    @Test
    @DisplayName("用户画像为空时返回空页")
    void recommendByProfileNullProfileReturnsEmptyPage() {
        doReturn(null).when(userProfileService).getProfile("user-1");

        Page<String> result = recommendService.recommendByProfile("user-1", 1, 10);

        assertThat(result.getRecords()).isEmpty();
    }

    @Test
    @DisplayName("根据兴趣分类推荐课程")
    void recommendByProfileWithInterestsReturnsCourseIds() {
        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("user-1")
                .interestCategoryIds(List.of("cat-1"))
                .build();
        doReturn(profile).when(userProfileService).getProfile("user-1");

        doReturn(Collections.emptyList()).when(userLearningRecordMapper).selectList(any(LambdaQueryWrapper.class));

        CategoryCourse cc = CategoryCourse.builder().courseId("c-1").categoryId("cat-1").build();
        doReturn(List.of(cc)).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));

        Course course = Course.builder().id("c-1").title("兴趣课程").build();
        Page<Course> coursePage = new Page<>(1, 10, 1);
        coursePage.setRecords(List.of(course));
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        Page<String> result = recommendService.recommendByProfile("user-1", 1, 10);

        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0)).isEqualTo("c-1");
    }

    @Test
    @DisplayName("兴趣分类无匹配课程时返回空页")
    void recommendByProfileNoInterestMatchReturnsEmptyPage() {
        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("user-1")
                .interestCategoryIds(List.of("cat-empty"))
                .build();
        doReturn(profile).when(userProfileService).getProfile("user-1");
        doReturn(Collections.emptyList()).when(userLearningRecordMapper).selectList(any(LambdaQueryWrapper.class));
        doReturn(Collections.emptyList()).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));

        Page<String> result = recommendService.recommendByProfile("user-1", 1, 10);

        assertThat(result.getRecords()).isEmpty();
    }

    @Test
    @DisplayName("答题正确率高于80%时推荐hard难度课程")
    void recommendByProfileWithHighCorrectRateFiltersHard() {
        UserProfileResponse.QuizStats quizStats = UserProfileResponse.QuizStats.builder()
                .correctRate(0.85).build();
        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("user-1")
                .interestCategoryIds(List.of("cat-1"))
                .quiz(quizStats)
                .build();
        doReturn(profile).when(userProfileService).getProfile("user-1");
        doReturn(Collections.emptyList()).when(userLearningRecordMapper).selectList(any(LambdaQueryWrapper.class));

        CategoryCourse cc = CategoryCourse.builder().courseId("c-1").categoryId("cat-1").build();
        doReturn(List.of(cc)).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));

        Course course = Course.builder().id("c-1").difficulty("hard").build();
        Page<Course> coursePage = new Page<>(1, 10, 1);
        coursePage.setRecords(List.of(course));
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        Page<String> result = recommendService.recommendByProfile("user-1", 1, 10);

        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    @DisplayName("答题正确率在50%-80%时推荐medium难度课程")
    void recommendByProfileWithMediumCorrectRateFiltersMedium() {
        UserProfileResponse.QuizStats quizStats = UserProfileResponse.QuizStats.builder()
                .correctRate(0.65).build();
        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("user-1")
                .interestCategoryIds(List.of("cat-1"))
                .quiz(quizStats)
                .build();
        doReturn(profile).when(userProfileService).getProfile("user-1");
        doReturn(Collections.emptyList()).when(userLearningRecordMapper).selectList(any(LambdaQueryWrapper.class));

        CategoryCourse cc = CategoryCourse.builder().courseId("c-1").categoryId("cat-1").build();
        doReturn(List.of(cc)).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));

        Course course = Course.builder().id("c-1").difficulty("medium").build();
        Page<Course> coursePage = new Page<>(1, 10, 1);
        coursePage.setRecords(List.of(course));
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        Page<String> result = recommendService.recommendByProfile("user-1", 1, 10);

        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    @DisplayName("答题正确率低于50%时推荐easy难度课程")
    void recommendByProfileWithLowCorrectRateFiltersEasy() {
        UserProfileResponse.QuizStats quizStats = UserProfileResponse.QuizStats.builder()
                .correctRate(0.35).build();
        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("user-1")
                .interestCategoryIds(List.of("cat-1"))
                .quiz(quizStats)
                .build();
        doReturn(profile).when(userProfileService).getProfile("user-1");
        doReturn(Collections.emptyList()).when(userLearningRecordMapper).selectList(any(LambdaQueryWrapper.class));

        CategoryCourse cc = CategoryCourse.builder().courseId("c-1").categoryId("cat-1").build();
        doReturn(List.of(cc)).when(categoryCourseMapper).selectList(any(LambdaQueryWrapper.class));

        Course course = Course.builder().id("c-1").difficulty("easy").build();
        Page<Course> coursePage = new Page<>(1, 10, 1);
        coursePage.setRecords(List.of(course));
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        Page<String> result = recommendService.recommendByProfile("user-1", 1, 10);

        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    @DisplayName("用户画像无兴趣分类时不过滤分类")
    void recommendByProfileNoInterestsSkipsCategoryFilter() {
        UserProfileResponse profile = UserProfileResponse.builder()
                .userId("user-1")
                .build();
        doReturn(profile).when(userProfileService).getProfile("user-1");
        doReturn(Collections.emptyList()).when(userLearningRecordMapper).selectList(any(LambdaQueryWrapper.class));

        Course course = Course.builder().id("c-1").build();
        Page<Course> coursePage = new Page<>(1, 10, 1);
        coursePage.setRecords(List.of(course));
        doReturn(coursePage).when(courseMapper).selectPage(any(Page.class), any(LambdaQueryWrapper.class));

        Page<String> result = recommendService.recommendByProfile("user-1", 1, 10);

        assertThat(result.getRecords()).hasSize(1);
    }

    // ==================== paginate ====================

    @Test
    @DisplayName("分页超过数据范围时返回空列表")
    void paginateBeyondRangeReturnsEmpty() {
        Page<String> cfPage = new Page<>(1, 10);
        cfPage.setRecords(List.of("c-1"));
        Page<String> graphPage = new Page<>(1, 10);
        Page<String> profilePage = new Page<>(1, 10);

        doReturn(cfPage).when(recommendService).recommendByCF(anyString(), anyInt(), anyInt());
        doReturn(graphPage).when(recommendService).recommendByGraph(anyString(), anyInt(), anyInt());
        doReturn(profilePage).when(recommendService).recommendByProfile(anyString(), anyInt(), anyInt());

        Page<String> result = recommendService.recommend("user-1", 10, 10);

        assertThat(result.getRecords()).isEmpty();
    }
}
