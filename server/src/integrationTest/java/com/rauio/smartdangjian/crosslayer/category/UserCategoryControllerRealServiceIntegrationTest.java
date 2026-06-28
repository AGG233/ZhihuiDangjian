package com.rauio.smartdangjian.crosslayer.category;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.category.controller.user.UserCategoryController;
import com.rauio.smartdangjian.server.category.pojo.response.CategoryResponse;
import com.rauio.smartdangjian.server.category.service.category.CategoryService;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryArticle;
import com.rauio.smartdangjian.server.content.service.article.ArticleService;
import com.rauio.smartdangjian.server.course.pojo.entity.CategoryCourse;
import com.rauio.smartdangjian.server.course.service.course.CourseService;

@SpringBootTest(classes = UserCategoryControllerRealServiceIntegrationTest.TestConfig.class)
@DisplayName("用户目录控制层真实 UserCategoryService 集成测试")
class UserCategoryControllerRealServiceIntegrationTest extends CrossLayerTestBase {

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private ArticleService articleService;

    @BeforeEach
    void setUp() {
        reset(categoryService, courseService, articleService);
        setStudentContext(1L, "uni-1");
    }

    @Test
    @DisplayName("GET /api/content/categories/{id} 成功返回分类详情")
    void getCategoryById() throws Exception {
        CategoryResponse response = new CategoryResponse();
        response.setId(100L);
        response.setName("党建知识");
        response.setUniversityId("uni-1");
        when(categoryService.get(100L)).thenReturn(response);

        mockMvc.perform(get("/api/content/categories/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value("100"))
                .andExpect(jsonPath("$.data.name").value("党建知识"));

        verify(categoryService).get(100L);
    }

    @Test
    @DisplayName("GET /api/content/categories 成功返回根分类列表")
    void getRootList() throws Exception {
        CategoryResponse root = new CategoryResponse();
        root.setId(1L);
        root.setName("根分类");
        when(categoryService.getRootList()).thenReturn(List.of(root));

        mockMvc.perform(get("/api/content/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].id").value("1"))
                .andExpect(jsonPath("$.data[0].name").value("根分类"));

        verify(categoryService).getRootList();
    }

    @Test
    @DisplayName("GET /api/content/categories/root 成功返回根分类列表")
    void getRootListExplicit() throws Exception {
        when(categoryService.getRootList()).thenReturn(List.of());

        mockMvc.perform(get("/api/content/categories/root"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").isArray());

        verify(categoryService).getRootList();
    }

    @Test
    @DisplayName("GET /api/content/categories/{id}/children 成功返回子分类列表")
    void getChildren() throws Exception {
        CategoryResponse child = new CategoryResponse();
        child.setId(2L);
        child.setName("子分类");
        child.setParentId(1L);
        when(categoryService.getByParentId(1L)).thenReturn(List.of(child));

        mockMvc.perform(get("/api/content/categories/1/children"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].id").value("2"))
                .andExpect(jsonPath("$.data[0].parentId").value("1"));

        verify(categoryService).getByParentId(1L);
    }

    @Test
    @DisplayName("GET /api/content/categories/{categoryId}/courses 成功返回分类课程")
    void getCoursesByCategory() throws Exception {
        CategoryCourse categoryCourse =
                CategoryCourse.builder().categoryId(1L).courseId(100L).build();
        when(courseService.getByCategoryId(1L)).thenReturn(List.of(categoryCourse));

        mockMvc.perform(get("/api/content/categories/1/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].categoryId").value(1))
                .andExpect(jsonPath("$.data[0].courseId").value("100"));

        verify(courseService).getByCategoryId(1L);
    }

    @Test
    @DisplayName("GET /api/content/categories/{categoryId}/articles 成功返回分类文章")
    void getArticlesByCategory() throws Exception {
        CategoryArticle categoryArticle = new CategoryArticle();
        categoryArticle.setCategoryId(1L);
        categoryArticle.setArticleId(200L);
        when(articleService.getByCategoryId(1L)).thenReturn(List.of(categoryArticle));

        mockMvc.perform(get("/api/content/categories/1/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].categoryId").value(1))
                .andExpect(jsonPath("$.data[0].articleId").value("200"));

        verify(articleService).getByCategoryId(1L);
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        UserCategoryController userCategoryController(
                CategoryService categoryService, CourseService courseService, ArticleService articleService) {
            return new UserCategoryController(categoryService, courseService, articleService);
        }

        @Bean
        AbstractPlatformTransactionManager transactionManager() {
            return new AbstractPlatformTransactionManager() {
                @Override
                protected Object doGetTransaction() {
                    return new Object();
                }

                @Override
                protected void doBegin(Object transaction, TransactionDefinition definition) {}

                @Override
                protected void doCommit(DefaultTransactionStatus status) {}

                @Override
                protected void doRollback(DefaultTransactionStatus status) {}
            };
        }
    }
}
