package com.rauio.smartdangjian.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import com.rauio.smartdangjian.exception.GlobalExceptionHandler;
import com.rauio.smartdangjian.server.ai.controller.admin.AdminFaqController;
import com.rauio.smartdangjian.server.ai.controller.admin.AdminPromptController;
import com.rauio.smartdangjian.server.ai.controller.admin.AdminSkillController;
import com.rauio.smartdangjian.server.ai.controller.user.UserChatController;
import com.rauio.smartdangjian.server.ai.service.AiMemoryService;
import com.rauio.smartdangjian.server.ai.service.FaqService;
import com.rauio.smartdangjian.server.ai.service.LLMService;
import com.rauio.smartdangjian.server.ai.service.PromptService;
import com.rauio.smartdangjian.server.ai.service.SkillService;
import com.rauio.smartdangjian.server.auth.config.SaTokenConfigure;
import com.rauio.smartdangjian.server.auth.controller.AuthController;
import com.rauio.smartdangjian.server.auth.service.AuthService;
import com.rauio.smartdangjian.server.auth.service.CaptchaService;
import com.rauio.smartdangjian.server.category.controller.admin.AdminCategoryController;
import com.rauio.smartdangjian.server.chapter.controller.admin.AdminChapterController;
import com.rauio.smartdangjian.server.content.controller.admin.AdminContentController;
import com.rauio.smartdangjian.server.course.controller.admin.AdminCourseController;
import com.rauio.smartdangjian.server.category.controller.user.UserCategoryController;
import com.rauio.smartdangjian.server.chapter.controller.user.UserChapterController;
import com.rauio.smartdangjian.server.content.controller.user.UserContentController;
import com.rauio.smartdangjian.server.course.controller.user.UserCourseController;
import com.rauio.smartdangjian.server.content.service.ChapterContentBlockService;
import com.rauio.smartdangjian.server.content.service.article.ArticleService;
import com.rauio.smartdangjian.server.category.service.category.CategoryService;
import com.rauio.smartdangjian.server.chapter.service.chapter.ChapterService;
import com.rauio.smartdangjian.server.course.service.course.CourseService;
import com.rauio.smartdangjian.server.graph.controller.admin.AdminPartyHistoryController;
import com.rauio.smartdangjian.server.graph.controller.user.UserKnowledgeGraphController;
import com.rauio.smartdangjian.server.graph.controller.user.UserPartyHistoryController;
import com.rauio.smartdangjian.server.graph.service.KnowledgeGraphService;
import com.rauio.smartdangjian.server.graph.service.PartyHistoryGraphService;
import com.rauio.smartdangjian.server.graph.service.PartyHistoryImportService;
import com.rauio.smartdangjian.server.graph.service.PartyHistoryQueryService;
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
import com.rauio.smartdangjian.server.search.controller.LearningHotspotController;
import com.rauio.smartdangjian.server.search.controller.SearchController;
import com.rauio.smartdangjian.server.search.service.LearningHotspotService;
import com.rauio.smartdangjian.server.search.service.RecommendService;
import com.rauio.smartdangjian.server.search.service.SearchService;
import com.rauio.smartdangjian.server.search.service.UserProfileService;
import com.rauio.smartdangjian.server.user.controller.admin.AdminUserController;
import com.rauio.smartdangjian.server.user.controller.user.UserController;
import com.rauio.smartdangjian.server.user.service.UserService;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.handler.SaAnnotationHandlerInterface;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.strategy.SaAnnotationStrategy;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = AbstractSecurityAuthorizationIntegrationTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = true)
@TestPropertySource(
        locations = "classpath:application-test.yaml",
        properties = {"REDIS_HOST=localhost", "REDIS_PORT=6379", "REDIS_DATABASE=0"})
abstract class AbstractSecurityAuthorizationIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @MockitoBean
    protected AuthService authService;

    @MockitoBean
    protected CaptchaService captchaService;

    @MockitoBean
    protected UserService userService;

    @MockitoBean
    protected SearchService searchService;

    @MockitoBean
    protected RecommendService recommendService;

    @MockitoBean
    protected UserProfileService userProfileService;

    @MockitoBean
    protected LearningHotspotService learningHotspotService;

    @MockitoBean
    protected FileService fileService;

    @MockitoBean
    protected KnowledgeGraphService knowledgeGraphService;

    @MockitoBean
    protected PartyHistoryQueryService partyHistoryQueryService;

    @MockitoBean
    protected PartyHistoryImportService partyHistoryImportService;

    @MockitoBean
    protected PartyHistoryGraphService partyHistoryGraphService;

    @MockitoBean
    protected ChapterContentBlockService chapterContentBlockService;

    @MockitoBean
    protected CategoryService categoryService;

    @MockitoBean
    protected ArticleService articleService;

    @MockitoBean
    protected ChapterService chapterService;

    @MockitoBean
    protected CourseService courseService;

    @MockitoBean
    protected QuizService quizService;

    @MockitoBean
    protected QuizOptionService quizOptionService;

    @MockitoBean
    protected UserQuizAnswerService userQuizAnswerService;

    @MockitoBean
    protected PromptService promptService;

    @MockitoBean
    protected SkillService skillService;

    @MockitoBean
    protected FaqService faqService;

    @MockitoBean
    protected UserLearningRecordService learningRecordService;

    @MockitoBean
    protected UserChapterProgressService chapterProgressService;

    @MockitoBean
    protected ResourceMetaService resourceMetaService;

    @MockitoBean
    protected BannerService bannerService;

    @MockitoBean
    protected LLMService llmService;

    @MockitoBean
    protected AiMemoryService aiMemoryService;

    @BeforeEach
    void resetMocks() {
        org.mockito.Mockito.reset(
                authService,
                captchaService,
                userService,
                searchService,
                recommendService,
                userProfileService,
                learningHotspotService,
                fileService,
                knowledgeGraphService,
                partyHistoryQueryService,
                partyHistoryImportService,
                partyHistoryGraphService,
                chapterContentBlockService,
                categoryService,
                articleService,
                chapterService,
                courseService,
                quizService,
                quizOptionService,
                userQuizAnswerService,
                promptService,
                skillService,
                faqService,
                learningRecordService,
                chapterProgressService,
                resourceMetaService,
                bannerService,
                llmService,
                aiMemoryService);
    }

    protected static SaAnnotationHandlerInterface<SaCheckLogin> allowingLoginHandler() {
        return new SaAnnotationHandlerInterface<>() {
            @Override
            public Class<SaCheckLogin> getHandlerAnnotationClass() {
                return SaCheckLogin.class;
            }

            @Override
            public void checkMethod(SaCheckLogin at, AnnotatedElement element) {
                // Explicitly pass login annotations so role/permission tests isolate their target branch.
            }
        };
    }

    protected static SaAnnotationHandlerInterface<SaCheckRole> rejectingRoleHandler(String expectedRole) {
        return new SaAnnotationHandlerInterface<>() {
            @Override
            public Class<SaCheckRole> getHandlerAnnotationClass() {
                return SaCheckRole.class;
            }

            @Override
            public void checkMethod(SaCheckRole at, java.lang.reflect.AnnotatedElement element) {
                if (expectedRole != null) {
                    org.assertj.core.api.Assertions.assertThat(at.value()).containsExactly(expectedRole);
                }
                throw new NotRoleException(expectedRole);
            }
        };
    }

    protected static SaAnnotationHandlerInterface<SaCheckRole> allowingRoleHandler(String expectedRole) {
        return new SaAnnotationHandlerInterface<>() {
            @Override
            public Class<SaCheckRole> getHandlerAnnotationClass() {
                return SaCheckRole.class;
            }

            @Override
            public void checkMethod(SaCheckRole at, java.lang.reflect.AnnotatedElement element) {
                if (expectedRole != null) {
                    org.assertj.core.api.Assertions.assertThat(at.value()).containsExactly(expectedRole);
                }
            }
        };
    }

    protected static SaAnnotationHandlerInterface<SaCheckPermission> rejectingPermissionHandler(
            String expectedPermission) {
        return new SaAnnotationHandlerInterface<>() {
            @Override
            public Class<SaCheckPermission> getHandlerAnnotationClass() {
                return SaCheckPermission.class;
            }

            @Override
            public void checkMethod(SaCheckPermission at, java.lang.reflect.AnnotatedElement element) {
                if (expectedPermission != null) {
                    org.assertj.core.api.Assertions.assertThat(at.value()).containsExactly(expectedPermission);
                }
                throw new NotPermissionException(expectedPermission);
            }
        };
    }

    protected static SaAnnotationHandlerInterface<SaCheckPermission> allowingPermissionHandler(
            String expectedPermission) {
        return new SaAnnotationHandlerInterface<>() {
            @Override
            public Class<SaCheckPermission> getHandlerAnnotationClass() {
                return SaCheckPermission.class;
            }

            @Override
            public void checkMethod(SaCheckPermission at, java.lang.reflect.AnnotatedElement element) {
                if (expectedPermission != null) {
                    org.assertj.core.api.Assertions.assertThat(at.value()).containsExactly(expectedPermission);
                }
            }
        };
    }

    @SafeVarargs
    protected static AnnotationHandlerScope annotationHandlerScope(
            SaAnnotationHandlerInterface<? extends Annotation>... handlers) {
        return new AnnotationHandlerScope(handlers);
    }

    protected static final class AnnotationHandlerScope implements AutoCloseable {
        private final Map<Class<?>, SaAnnotationHandlerInterface<?>> originals = new LinkedHashMap<>();

        @SafeVarargs
        private AnnotationHandlerScope(SaAnnotationHandlerInterface<? extends Annotation>... handlers) {
            for (SaAnnotationHandlerInterface<? extends Annotation> handler : handlers) {
                Class<?> annotationClass = handler.getHandlerAnnotationClass();
                originals.put(annotationClass, SaAnnotationStrategy.instance.annotationHandlerMap.get(annotationClass));
                SaAnnotationStrategy.instance.annotationHandlerMap.put(annotationClass, handler);
            }
        }

        @Override
        public void close() {
            originals.forEach((annotationClass, handler) -> {
                if (handler == null) {
                    SaAnnotationStrategy.instance.annotationHandlerMap.remove(annotationClass);
                } else {
                    SaAnnotationStrategy.instance.annotationHandlerMap.put(annotationClass, handler);
                }
            });
        }
    }

    protected org.springframework.test.web.servlet.ResultActions perform(String path, String method) throws Exception {
        return switch (method) {
            case "GET" -> mockMvc.perform(get(path));
            case "POST" -> mockMvc.perform(post(path));
            case "PUT" ->
                mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put(path)
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{}"));
            case "DELETE" -> mockMvc.perform(delete(path));
            default -> throw new IllegalArgumentException("Unsupported method: " + method);
        };
    }

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
    @Import({SaTokenConfigure.class, GlobalExceptionHandler.class})
    static class TestConfig {
        @Bean
        com.rauio.smartdangjian.security.CurrentUserProvider currentUserProvider() {
            return new com.rauio.smartdangjian.security.CurrentUserProvider() {
                @Override
                public String getCurrentUserId() {
                    return cn.dev33.satoken.stp.StpUtil.getLoginIdAsString();
                }

                @Override
                public String getCurrentUserRole() {
                    return null;
                }

                @Override
                public boolean hasRole(String role) {
                    return cn.dev33.satoken.stp.StpUtil.hasRole(role);
                }

                @Override
                public com.rauio.smartdangjian.security.LoginUser getCurrentUser() {
                    return null;
                }
            };
        }

        @Bean
        AuthController authController(AuthService authService, CaptchaService captchaService) {
            return new AuthController(authService, captchaService);
        }

        @Bean
        AdminUserController adminUserController(UserService userService) {
            return new AdminUserController(userService);
        }

        @Bean
        UserController userController(UserService userService) {
            return new UserController(userService);
        }

        @Bean
        AdminCategoryController adminCategoryController(CategoryService categoryService) {
            return new AdminCategoryController(categoryService);
        }

        @Bean
        UserCategoryController userCategoryController(
                CategoryService categoryService, CourseService courseService, ArticleService articleService) {
            return new UserCategoryController(categoryService, courseService, articleService);
        }

        @Bean
        UserContentController userContentController(ChapterContentBlockService blockService) {
            return new UserContentController(blockService);
        }

        @Bean
        UserBannerController userBannerController(BannerService bannerService) {
            return new UserBannerController(bannerService);
        }

        @Bean
        AdminContentController adminContentController(ChapterContentBlockService blockService) {
            return new AdminContentController(blockService);
        }

        @Bean
        AdminChapterController adminChapterController(ChapterService chapterService) {
            return new AdminChapterController(chapterService);
        }

        @Bean
        AdminCourseController adminCourseController(CourseService courseService) {
            return new AdminCourseController(courseService);
        }

        @Bean
        UserCourseController userCourseController(
                CourseService courseService, com.rauio.smartdangjian.security.CurrentUserProvider currentUserProvider) {
            return new UserCourseController(courseService, currentUserProvider);
        }

        @Bean
        UserChapterController userChapterController(ChapterService chapterService) {
            return new UserChapterController(chapterService);
        }

        @Bean
        AdminQuizController adminQuizController(QuizService quizService, QuizOptionService quizOptionService) {
            return new AdminQuizController(quizService, quizOptionService);
        }

        @Bean
        AdminQuizAnswerController adminQuizAnswerController(UserQuizAnswerService answerService) {
            return new AdminQuizAnswerController(answerService);
        }

        @Bean
        UserQuizController userQuizController(QuizService quizService, QuizOptionService quizOptionService) {
            return new UserQuizController(quizService, quizOptionService);
        }

        @Bean
        UserQuizAnswerController userQuizAnswerController(
                UserQuizAnswerService answerService,
                com.rauio.smartdangjian.security.CurrentUserProvider currentUserProvider) {
            return new UserQuizAnswerController(answerService, currentUserProvider);
        }

        @Bean
        SearchController searchController(
                SearchService searchService, RecommendService recommendService, UserProfileService userProfileService) {
            return new SearchController(searchService, recommendService, userProfileService);
        }

        @Bean
        FileController fileController(
                FileService fileService, com.rauio.smartdangjian.security.CurrentUserProvider currentUserProvider) {
            return new FileController(fileService, currentUserProvider);
        }

        @Bean
        UserKnowledgeGraphController userKnowledgeGraphController(KnowledgeGraphService knowledgeGraphService) {
            return new UserKnowledgeGraphController(knowledgeGraphService);
        }

        @Bean
        AdminPartyHistoryController adminPartyHistoryController(
                PartyHistoryImportService importService, PartyHistoryGraphService graphService) {
            return new AdminPartyHistoryController(importService, graphService);
        }

        @Bean
        UserPartyHistoryController userPartyHistoryController(PartyHistoryQueryService queryService) {
            return new UserPartyHistoryController(queryService);
        }

        @Bean
        LearningHotspotController learningHotspotController(LearningHotspotService learningHotspotService) {
            return new LearningHotspotController(learningHotspotService);
        }

        @Bean
        UserLearningRecordController userLearningRecordController(
                UserLearningRecordService recordService,
                com.rauio.smartdangjian.security.CurrentUserProvider currentUserProvider) {
            return new UserLearningRecordController(recordService, currentUserProvider);
        }

        @Bean
        UserLearningGraphSyncController userLearningGraphSyncController(
                UserLearningRecordService recordService,
                com.rauio.smartdangjian.security.CurrentUserProvider currentUserProvider) {
            return new UserLearningGraphSyncController(recordService, currentUserProvider);
        }

        @Bean
        UserChapterProgressController userChapterProgressController(
                UserChapterProgressService progressService,
                com.rauio.smartdangjian.security.CurrentUserProvider currentUserProvider) {
            return new UserChapterProgressController(progressService, currentUserProvider);
        }

        @Bean
        AdminPromptController adminPromptController(PromptService promptService) {
            return new AdminPromptController(promptService);
        }

        @Bean
        AdminSkillController adminSkillController(SkillService skillService) {
            return new AdminSkillController(skillService);
        }

        @Bean
        AdminFaqController adminFaqController(FaqService faqService) {
            return new AdminFaqController(faqService);
        }

        @Bean
        AdminLearningRecordController adminLearningRecordController(UserLearningRecordService recordService) {
            return new AdminLearningRecordController(recordService);
        }

        @Bean
        AdminChapterProgressController adminChapterProgressController(UserChapterProgressService progressService) {
            return new AdminChapterProgressController(progressService);
        }

        @Bean
        AdminResourceMetaController adminResourceMetaController(ResourceMetaService resourceMetaService) {
            return new AdminResourceMetaController(resourceMetaService);
        }

        @Bean
        AdminBannerController adminBannerController(BannerService bannerService) {
            return new AdminBannerController(bannerService);
        }

        @Bean
        UserChatController userChatController(
                LLMService llmService, AiMemoryService aiMemoryService, UserService userService) {
            return new UserChatController(llmService, aiMemoryService, userService);
        }
    }
}
