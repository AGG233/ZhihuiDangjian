package com.rauio.smartdangjian.crosslayer.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.lenient;

import java.util.List;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.content.mapper.CategoryCourseMapper;
import com.rauio.smartdangjian.server.content.mapper.ChapterMapper;
import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.content.pojo.entity.Chapter;
import com.rauio.smartdangjian.server.learning.mapper.UserChapterProgressMapper;
import com.rauio.smartdangjian.server.learning.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserChapterProgress;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.search.service.RecommendService;
import com.rauio.smartdangjian.server.search.service.UserProfileService;
import com.rauio.smartdangjian.server.user.mapper.UserSimilarityMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.UserSimilarity;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.server.user.service.UserSimilarityService;

/**
 * 协同过滤推荐跨层回归测试。
 *
 * <p>装配真实 {@link RecommendService}（构造器直查 mapper），验证修复后的加权语义：
 * 课程得分按邻居相似度加权累加，高相似邻居推荐的课程排名更靠前；相似度缺失的邻居
 * 行为不计分。回归背景：历史实现固定计数累加（未乘相似度权重），导致排序与相似度脱钩。
 */
@SpringBootTest(classes = RecommendCFFlowTest.TestConfig.class)
@TestPropertySource(properties = {"spring.ai.model.embedding=dashscope", "spring.ai.vectorstore.type=none"})
@DisplayName("协同过滤推荐跨层回归")
class RecommendCFFlowTest extends CrossLayerTestBase {

    @MockitoBean
    private UserLearningRecordMapper learningRecordMapper;

    @MockitoBean
    private UserChapterProgressMapper userChapterProgressMapper;

    @MockitoBean
    private UserSimilarityMapper userSimilarityMapper;

    @MockitoBean
    private ChapterMapper chapterMapper;

    @MockitoBean
    private CategoryCourseMapper categoryCourseMapper;

    @MockitoBean
    private CourseMapper courseMapper;

    @MockitoBean
    private UserSimilarityService userSimilarityService;

    @MockitoBean
    private Neo4jClient neo4jClient;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private UserProfileService userProfileService;

    @Autowired
    private RecommendService recommendService;

    @BeforeEach
    void stubDefaults() {
        lenient().when(userProfileService.getProfile(any())).thenReturn(null);
        lenient().when(chapterMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        lenient().when(courseMapper.selectList(any(LambdaQueryWrapper.class))).thenReturn(List.of());
        lenient()
                .when(categoryCourseMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());
        lenient()
                .when(userChapterProgressMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of());
    }

    @BeforeAll
    static void initMybatisPlus() {
        MybatisConfiguration config = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(config, "");
        TableInfoHelper.initTableInfo(assistant, Chapter.class);
        TableInfoHelper.initTableInfo(assistant, UserLearningRecord.class);
        TableInfoHelper.initTableInfo(assistant, UserChapterProgress.class);
        TableInfoHelper.initTableInfo(assistant, UserSimilarity.class);
    }

    @Test
    @DisplayName("高相似邻居推荐的课程在加权后排在低相似邻居课程之前")
    void highSimilarityNeighborCoursesRankFirst() {
        Page<UserSimilarity> similarityPage = new Page<>(1, 10, 2);
        similarityPage.setRecords(List.of(
                UserSimilarity.builder()
                        .userId1(1L)
                        .userId2(2L)
                        .similarityScore(new java.math.BigDecimal("0.9"))
                        .build(),
                UserSimilarity.builder()
                        .userId1(1L)
                        .userId2(3L)
                        .similarityScore(new java.math.BigDecimal("0.1"))
                        .build()));
        lenient()
                .when(userSimilarityMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(similarityPage);

        // 第一次查询：当前用户自身记录（用于排除已学课程）；第二次查询：邻居行为记录
        UserLearningRecord own =
                UserLearningRecord.builder().chapterId(99L).userId(1L).build();
        UserLearningRecord peer2Record =
                UserLearningRecord.builder().chapterId(10L).userId(2L).build();
        UserLearningRecord peer3Record =
                UserLearningRecord.builder().chapterId(20L).userId(3L).build();
        lenient()
                .when(learningRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(own), List.of(peer2Record, peer3Record));

        // 章节→课程映射：章节10→课程100，章节20→课程200
        lenient()
                .when(chapterMapper.selectByIds(anyCollection()))
                .thenReturn(List.of(
                        Chapter.builder().id(10L).courseId(100L).build(),
                        Chapter.builder().id(20L).courseId(200L).build()));

        var result = recommendService.recommendByCF(1L, 1, 10);

        assertThat(result.getRecords()).containsExactly(100L, 200L);
    }

    @Test
    @DisplayName("无相似度分数的邻居学习行为不计入课程得分")
    void neighborWithoutScoreIsIgnored() {
        Page<UserSimilarity> similarityPage = new Page<>(1, 10, 1);
        similarityPage.setRecords(
                List.of(UserSimilarity.builder().userId1(1L).userId2(2L).build()));
        lenient()
                .when(userSimilarityMapper.selectPage(any(Page.class), any(LambdaQueryWrapper.class)))
                .thenReturn(similarityPage);

        UserLearningRecord own =
                UserLearningRecord.builder().chapterId(99L).userId(1L).build();
        UserLearningRecord peerRecord =
                UserLearningRecord.builder().chapterId(10L).userId(2L).build();
        lenient()
                .when(learningRecordMapper.selectList(any(LambdaQueryWrapper.class)))
                .thenReturn(List.of(own), List.of(peerRecord));

        lenient()
                .when(chapterMapper.selectByIds(anyCollection()))
                .thenReturn(List.of(Chapter.builder().id(10L).courseId(100L).build()));

        var result = recommendService.recommendByCF(1L, 1, 10);

        assertThat(result.getRecords()).isEmpty();
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        RecommendService recommendService(
                UserLearningRecordMapper userLearningRecordMapper,
                UserChapterProgressMapper userChapterProgressMapper,
                UserSimilarityMapper userSimilarityMapper,
                ChapterMapper chapterMapper,
                CategoryCourseMapper categoryCourseMapper,
                CourseMapper courseMapper,
                UserSimilarityService userSimilarityService,
                Neo4jClient neo4jClient,
                UserProfileService userProfileService) {
            return new RecommendService(
                    userLearningRecordMapper,
                    userChapterProgressMapper,
                    userSimilarityMapper,
                    chapterMapper,
                    categoryCourseMapper,
                    courseMapper,
                    userSimilarityService,
                    neo4jClient,
                    userProfileService);
        }
    }
}
