package com.rauio.smartdangjian;

import static org.mockito.Mockito.mock;

import org.mockito.Mockito;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.security.LoginUser;
import com.rauio.smartdangjian.server.ai.controller.admin.AdminPromptController;
import com.rauio.smartdangjian.server.ai.controller.admin.AdminSkillController;
import com.rauio.smartdangjian.server.ai.controller.user.UserChatController;
import com.rauio.smartdangjian.server.ai.service.AiMemoryService;
import com.rauio.smartdangjian.server.ai.service.LLMService;
import com.rauio.smartdangjian.server.ai.service.PromptService;
import com.rauio.smartdangjian.server.ai.service.SkillService;
import com.rauio.smartdangjian.server.auth.controller.AuthController;
import com.rauio.smartdangjian.server.auth.service.AuthService;
import com.rauio.smartdangjian.server.auth.service.CaptchaService;
import com.rauio.smartdangjian.server.content.controller.admin.AdminCategoryController;
import com.rauio.smartdangjian.server.content.controller.admin.AdminChapterController;
import com.rauio.smartdangjian.server.content.controller.admin.AdminContentController;
import com.rauio.smartdangjian.server.content.controller.admin.AdminCourseController;
import com.rauio.smartdangjian.server.content.controller.user.UserCategoryController;
import com.rauio.smartdangjian.server.content.controller.user.UserChapterController;
import com.rauio.smartdangjian.server.content.controller.user.UserContentController;
import com.rauio.smartdangjian.server.content.controller.user.UserCourseController;
import com.rauio.smartdangjian.server.content.service.article.ArticleService;
import com.rauio.smartdangjian.server.content.service.category.CategoryService;
import com.rauio.smartdangjian.server.content.service.ChapterContentBlockService;
import com.rauio.smartdangjian.server.content.service.course.CourseService;
import com.rauio.smartdangjian.server.content.service.chapter.ChapterService;
import com.rauio.smartdangjian.server.graph.controller.user.UserKnowledgeGraphController;
import com.rauio.smartdangjian.server.graph.service.KnowledgeGraphService;
import com.rauio.smartdangjian.server.learning.controller.admin.AdminChapterProgressController;
import com.rauio.smartdangjian.server.learning.controller.admin.AdminLearningRecordController;
import com.rauio.smartdangjian.server.learning.controller.user.UserChapterProgressController;
import com.rauio.smartdangjian.server.learning.controller.user.UserLearningGraphSyncController;
import com.rauio.smartdangjian.server.learning.controller.user.UserLearningRecordController;
import com.rauio.smartdangjian.server.learning.service.UserChapterProgressService;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;
import com.rauio.smartdangjian.server.quiz.controller.admin.AdminQuizAnswerController;
import com.rauio.smartdangjian.server.quiz.controller.admin.AdminQuizController;
import com.rauio.smartdangjian.server.quiz.controller.user.UserQuizAnswerController;
import com.rauio.smartdangjian.server.quiz.controller.user.UserQuizController;
import com.rauio.smartdangjian.server.quiz.service.QuizOptionService;
import com.rauio.smartdangjian.server.quiz.service.QuizService;
import com.rauio.smartdangjian.server.quiz.service.UserQuizAnswerService;
import com.rauio.smartdangjian.server.resource.controller.admin.AdminBannerController;
import com.rauio.smartdangjian.server.resource.controller.admin.AdminResourceMetaController;
import com.rauio.smartdangjian.server.resource.controller.user.FileController;
import com.rauio.smartdangjian.server.resource.controller.user.UserBannerController;
import com.rauio.smartdangjian.server.resource.service.BannerService;
import com.rauio.smartdangjian.server.resource.service.FileService;
import com.rauio.smartdangjian.server.resource.service.ResourceMetaService;
import com.rauio.smartdangjian.server.search.controller.SearchController;
import com.rauio.smartdangjian.server.search.service.RecommendService;
import com.rauio.smartdangjian.server.search.service.SearchService;
import com.rauio.smartdangjian.server.search.service.UserProfileService;
import com.rauio.smartdangjian.server.user.controller.admin.AdminUserController;
import com.rauio.smartdangjian.server.user.controller.publicapi.ApiController;
import com.rauio.smartdangjian.server.user.controller.user.UserController;
import com.rauio.smartdangjian.server.user.service.UniversitiesService;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.server.user.mapper.UniversitiesMapper;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.mapper.UserSimilarityMapper;
import com.rauio.smartdangjian.server.content.mapper.CourseMapper;
import com.rauio.smartdangjian.server.content.mapper.CategoryCourseMapper;
import com.rauio.smartdangjian.server.content.mapper.CategoryMapper;
import com.rauio.smartdangjian.server.content.mapper.ChapterMapper;
import com.rauio.smartdangjian.server.content.mapper.ChapterContentBlockMapper;
import com.rauio.smartdangjian.server.content.mapper.ArticleContentBlockMapper;
import com.rauio.smartdangjian.server.content.mapper.ArticleMapper;
import com.rauio.smartdangjian.server.content.mapper.CategoryArticleMapper;
import com.rauio.smartdangjian.server.resource.mapper.ResourceMetaMapper;
import com.rauio.smartdangjian.server.quiz.mapper.QuizMapper;
import com.rauio.smartdangjian.server.quiz.mapper.QuizOptionMapper;
import com.rauio.smartdangjian.server.quiz.mapper.UserQuizAnswerMapper;
import com.rauio.smartdangjian.server.learning.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.server.learning.mapper.UserChapterProgressMapper;
import com.rauio.smartdangjian.server.ai.mapper.AiSkillMapper;
import com.rauio.smartdangjian.server.ai.mapper.AiPromptsMapper;
import com.rauio.smartdangjian.server.ai.mapper.AiFaqMapper;
import com.rauio.smartdangjian.server.ai.mapper.AiChatMessageMapper;

import cn.dev33.satoken.stp.StpUtil;

/**
 * 共享的 Controller 测试配置。
 *
 * <p>所有 Controller MockMvc 测试共用此 ApplicationContext，将 30+ 个独立上下文合并为 1 个，
 * 大幅节省测试启动时间。</p>
 *
 * <p>所有 Service 依赖提供默认 Mockito mock，每个测试类通过 {@code @MockitoBean} 覆盖
 * 各自需要的 Service mock，Spring 自动注入到 Controller Bean 的构造函数参数中。</p>
 */
@SpringBootConfiguration
@EnableWebMvc
@EnableAutoConfiguration(
        exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            org.redisson.spring.starter.RedissonAutoConfigurationV2.class,
            org.springframework.boot.autoconfigure.cache.CacheAutoConfiguration.class,
            org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration.class,
            org.springframework.boot.autoconfigure.neo4j.Neo4jAutoConfiguration.class,
            org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration.class,
            org.springframework.boot.autoconfigure.data.neo4j.Neo4jDataAutoConfiguration.class,
            org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration.class,
            cn.dev33.satoken.dao.SaTokenDaoForRedisTemplate.class,
            com.rauio.smartdangjian.config.RedisConfig.class,
            com.rauio.smartdangjian.config.TransactionConfig.class,
            com.rauio.smartdangjian.config.SensitiveWordConfig.class
        })
public class ControllerTestConfiguration {

    // ==================== CurrentUserProvider ====================

    @Bean
    CurrentUserProvider currentUserProvider() {
        return new CurrentUserProvider() {
            @Override
            public String getCurrentUserId() {
                return StpUtil.getLoginIdAsString();
            }

            @Override
            public String getCurrentUserRole() {
                return null;
            }

            @Override
            public boolean hasRole(String role) {
                return StpUtil.hasRole(role);
            }

            @Override
            public LoginUser getCurrentUser() {
                return null;
            }
        };
    }

    // ==================== 默认 Service & Mapper mock  ====================
    // 每个测试类通过 @MockitoBean 覆盖具体需要的 Service
    // ============================================================

    @Bean
    @Primary
    UserMapper userMapper() {
        return Mockito.mock(UserMapper.class);
    }

    @Bean
    @Primary
    UniversitiesMapper universitiesMapper() {
        return Mockito.mock(UniversitiesMapper.class);
    }

    @Bean
    @Primary
    UserSimilarityMapper userSimilarityMapper() {
        return Mockito.mock(UserSimilarityMapper.class);
    }

    @Bean
    @Primary
    CourseMapper courseMapper() {
        return Mockito.mock(CourseMapper.class);
    }

    @Bean
    @Primary
    CategoryCourseMapper categoryCourseMapper() {
        return Mockito.mock(CategoryCourseMapper.class);
    }

    @Bean
    @Primary
    CategoryMapper categoryMapper() {
        return Mockito.mock(CategoryMapper.class);
    }

    @Bean
    @Primary
    ChapterMapper chapterMapper() {
        return Mockito.mock(ChapterMapper.class);
    }

    @Bean
    @Primary
    ChapterContentBlockMapper chapterContentBlockMapper() {
        return Mockito.mock(ChapterContentBlockMapper.class);
    }

    @Bean
    @Primary
    ArticleContentBlockMapper articleContentBlockMapper() {
        return Mockito.mock(ArticleContentBlockMapper.class);
    }

    @Bean
    @Primary
    ArticleMapper articleMapper() {
        return Mockito.mock(ArticleMapper.class);
    }

    @Bean
    @Primary
    CategoryArticleMapper categoryArticleMapper() {
        return Mockito.mock(CategoryArticleMapper.class);
    }

    @Bean
    @Primary
    ResourceMetaMapper resourceMetaMapper() {
        return Mockito.mock(ResourceMetaMapper.class);
    }

    @Bean
    @Primary
    QuizMapper quizMapper() {
        return Mockito.mock(QuizMapper.class);
    }

    @Bean
    @Primary
    QuizOptionMapper quizOptionMapper() {
        return Mockito.mock(QuizOptionMapper.class);
    }

    @Bean
    @Primary
    UserQuizAnswerMapper userQuizAnswerMapper() {
        return Mockito.mock(UserQuizAnswerMapper.class);
    }

    @Bean
    @Primary
    UserLearningRecordMapper userLearningRecordMapper() {
        return Mockito.mock(UserLearningRecordMapper.class);
    }

    @Bean
    @Primary
    UserChapterProgressMapper userChapterProgressMapper() {
        return Mockito.mock(UserChapterProgressMapper.class);
    }

    @Bean
    @Primary
    AiSkillMapper aiSkillMapper() {
        return Mockito.mock(AiSkillMapper.class);
    }

    @Bean
    @Primary
    AiPromptsMapper aiPromptsMapper() {
        return Mockito.mock(AiPromptsMapper.class);
    }

    @Bean
    @Primary
    AiFaqMapper aiFaqMapper() {
        return Mockito.mock(AiFaqMapper.class);
    }

    @Bean
    @Primary
    AiChatMessageMapper aiChatMessageMapper() {
        return Mockito.mock(AiChatMessageMapper.class);
    }

    @Bean
    @Primary
    AuthService authService() {
        return Mockito.mock(AuthService.class);
    }

    @Bean
    @Primary
    CaptchaService captchaService() {
        return Mockito.mock(CaptchaService.class);
    }

    @Bean
    @Primary
    UserService userService() {
        return Mockito.mock(UserService.class);
    }

    @Bean
    @Primary
    UniversitiesService universitiesService() {
        return Mockito.mock(UniversitiesService.class);
    }

    @Bean
    @Primary
    CategoryService categoryService() {
        return Mockito.mock(CategoryService.class);
    }

    @Bean
    @Primary
    CourseService courseService() {
        return Mockito.mock(CourseService.class);
    }

    @Bean
    @Primary
    ArticleService articleService() {
        return Mockito.mock(ArticleService.class);
    }

    @Bean
    @Primary
    ChapterService chapterService() {
        return Mockito.mock(ChapterService.class);
    }

    @Bean
    @Primary
    ChapterContentBlockService chapterContentBlockService() {
        return Mockito.mock(ChapterContentBlockService.class);
    }

    @Bean
    @Primary
    BannerService bannerService() {
        return Mockito.mock(BannerService.class);
    }

    @Bean
    @Primary
    ResourceMetaService resourceMetaService() {
        return Mockito.mock(ResourceMetaService.class);
    }

    @Bean
    @Primary
    FileService fileService() {
        return Mockito.mock(FileService.class);
    }

    @Bean
    @Primary
    QuizService quizService() {
        return Mockito.mock(QuizService.class);
    }

    @Bean
    @Primary
    QuizOptionService quizOptionService() {
        return Mockito.mock(QuizOptionService.class);
    }

    @Bean
    @Primary
    UserQuizAnswerService userQuizAnswerService() {
        return Mockito.mock(UserQuizAnswerService.class);
    }

    @Bean
    @Primary
    SearchService searchService() {
        return Mockito.mock(SearchService.class);
    }

    @Bean
    @Primary
    RecommendService recommendService() {
        return Mockito.mock(RecommendService.class);
    }

    @Bean
    @Primary
    UserProfileService userProfileService() {
        return Mockito.mock(UserProfileService.class);
    }

    @Bean
    @Primary
    PromptService promptService() {
        return Mockito.mock(PromptService.class);
    }

    @Bean
    @Primary
    SkillService skillService() {
        return Mockito.mock(SkillService.class);
    }

    @Bean
    @Primary
    LLMService llmService() {
        return Mockito.mock(LLMService.class);
    }

    @Bean
    @Primary
    AiMemoryService aiMemoryService() {
        return Mockito.mock(AiMemoryService.class);
    }

    @Bean
    @Primary
    UserLearningRecordService userLearningRecordService() {
        return Mockito.mock(UserLearningRecordService.class);
    }

    @Bean
    @Primary
    UserChapterProgressService userChapterProgressService() {
        return Mockito.mock(UserChapterProgressService.class);
    }

    @Bean
    @Primary
    KnowledgeGraphService knowledgeGraphService() {
        return Mockito.mock(KnowledgeGraphService.class);
    }

    // ==================== Controller Bean ====================
    // Spring 自动从 @MockitoBean 或默认 mock 中注入 Service 参数
    // =========================================================

    // ========== Auth ==========

    @Bean
    AuthController authController(AuthService authService, CaptchaService captchaService) {
        return new AuthController(authService, captchaService);
    }

    // ========== User ==========

    @Bean
    AdminUserController adminUserController(UserService userService) {
        return new AdminUserController(userService);
    }

    @Bean
    UserController userController(UserService userService) {
        return new UserController(userService);
    }

    @Bean
    ApiController apiController(UniversitiesService universitiesService) {
        return new ApiController(universitiesService);
    }

    // ========== Category ==========

    @Bean
    AdminCategoryController adminCategoryController(CategoryService categoryService) {
        return new AdminCategoryController(categoryService);
    }

    @Bean
    UserCategoryController userCategoryController(
            CategoryService categoryService, CourseService courseService, ArticleService articleService) {
        return new UserCategoryController(categoryService, courseService, articleService);
    }

    // ========== Chapter ==========

    @Bean
    AdminChapterController adminChapterController(ChapterService chapterService) {
        return new AdminChapterController(chapterService);
    }

    @Bean
    UserChapterController userChapterController(ChapterService chapterService) {
        return new UserChapterController(chapterService);
    }

    // ========== Content ==========

    @Bean
    AdminContentController adminContentController(ChapterContentBlockService chapterContentBlockService) {
        return new AdminContentController(chapterContentBlockService);
    }

    @Bean
    UserContentController userContentController(ChapterContentBlockService chapterContentBlockService) {
        return new UserContentController(chapterContentBlockService);
    }

    // ========== Course ==========

    @Bean
    AdminCourseController adminCourseController(CourseService courseService) {
        return new AdminCourseController(courseService);
    }

    @Bean
    UserCourseController userCourseController(CourseService courseService, CurrentUserProvider currentUserProvider) {
        return new UserCourseController(courseService, currentUserProvider);
    }

    // ========== Resource ==========

    @Bean
    AdminBannerController adminBannerController(BannerService bannerService) {
        return new AdminBannerController(bannerService);
    }

    @Bean
    UserBannerController userBannerController(BannerService bannerService) {
        return new UserBannerController(bannerService);
    }

    @Bean
    AdminResourceMetaController adminResourceMetaController(ResourceMetaService resourceMetaService) {
        return new AdminResourceMetaController(resourceMetaService);
    }

    @Bean
    FileController fileController(FileService fileService, CurrentUserProvider currentUserProvider) {
        return new FileController(fileService, currentUserProvider);
    }

    // ========== Quiz ==========

    @Bean
    AdminQuizController adminQuizController(QuizService quizService, QuizOptionService quizOptionService) {
        return new AdminQuizController(quizService, quizOptionService);
    }

    @Bean
    UserQuizController userQuizController(QuizService quizService, QuizOptionService quizOptionService) {
        return new UserQuizController(quizService, quizOptionService);
    }

    @Bean
    AdminQuizAnswerController adminQuizAnswerController(UserQuizAnswerService userQuizAnswerService) {
        return new AdminQuizAnswerController(userQuizAnswerService);
    }

    @Bean
    UserQuizAnswerController userQuizAnswerController(
            UserQuizAnswerService userQuizAnswerService, CurrentUserProvider currentUserProvider) {
        return new UserQuizAnswerController(userQuizAnswerService, currentUserProvider);
    }

    // ========== Search ==========

    @Bean
    SearchController searchController(
            SearchService searchService, RecommendService recommendService, UserProfileService userProfileService) {
        return new SearchController(searchService, recommendService, userProfileService);
    }

    // ========== AI ==========

    @Bean
    AdminPromptController adminPromptController(PromptService promptService) {
        return new AdminPromptController(promptService);
    }

    @Bean
    AdminSkillController adminSkillController(SkillService skillService) {
        return new AdminSkillController(skillService);
    }

    @Bean
    UserChatController userChatController(
            LLMService llmService, AiMemoryService aiMemoryService, UserService userService) {
        return new UserChatController(llmService, aiMemoryService, userService);
    }

    // ========== Learning ==========

    @Bean
    AdminLearningRecordController adminLearningRecordController(UserLearningRecordService recordService) {
        return new AdminLearningRecordController(recordService);
    }

    @Bean
    UserLearningRecordController userLearningRecordController(
            UserLearningRecordService recordService, CurrentUserProvider currentUserProvider) {
        return new UserLearningRecordController(recordService, currentUserProvider);
    }

    @Bean
    AdminChapterProgressController adminChapterProgressController(UserChapterProgressService progressService) {
        return new AdminChapterProgressController(progressService);
    }

    @Bean
    UserChapterProgressController userChapterProgressController(
            UserChapterProgressService progressService, CurrentUserProvider currentUserProvider) {
        return new UserChapterProgressController(progressService, currentUserProvider);
    }

    @Bean
    UserLearningGraphSyncController userLearningGraphSyncController(
            UserLearningRecordService userLearningRecordService, CurrentUserProvider currentUserProvider) {
        return new UserLearningGraphSyncController(userLearningRecordService, currentUserProvider);
    }

    // ========== Graph ==========

    @Bean
    UserKnowledgeGraphController userKnowledgeGraphController(KnowledgeGraphService knowledgeGraphService) {
        return new UserKnowledgeGraphController(knowledgeGraphService);
    }
}
