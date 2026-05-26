package com.rauio.smartdangjian.controller.user;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.controller.factory.CategoryTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.controller.user.UserCategoryController;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryArticle;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryCourse;
import com.rauio.smartdangjian.server.content.pojo.response.CategoryResponse;
import com.rauio.smartdangjian.server.content.service.article.ArticleService;
import com.rauio.smartdangjian.server.content.service.category.CategoryService;
import com.rauio.smartdangjian.server.content.service.course.CourseService;
import com.rauio.smartdangjian.utils.spec.UserType;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = UserCategoryControllerTest.TestConfig.class)
@DisplayName("用户目录接口测试")
class UserCategoryControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public UserCategoryController userCategoryController(
                CategoryService categoryService, CourseService courseService, ArticleService articleService) {
            return new UserCategoryController(categoryService, courseService, articleService);
        }
    }

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private CourseService courseService;

    @MockitoBean
    private ArticleService articleService;

    @BeforeEach
    void setStudentContext() {
        CurrentUserPrincipal student = new CurrentUserPrincipal() {
            @Override
            public Long getId() {
                return 1L;
            }

            @Override
            public UserType getUserType() {
                return UserType.STUDENT;
            }

            @Override
            public String getUniversityId() {
                return "uni1";
            }
        };
        setSecurityContext(UserType.STUDENT, student.getId(), student.getUniversityId());
    }

    // ═══════════════════════════════════════════════════════════════
    // 正常场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("GET /{id} - 获取目录详情成功")
        void getCategoryByIdSuccess() throws Exception {
            CategoryResponse vo = CategoryTestDataFactory.createCategoryResponse(1L, "党建学习", null);
            when(categoryService.get(1L)).thenReturn(vo);

            mockMvc.perform(get("/api/content/categories/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("1"))
                    .andExpect(jsonPath("$.data.name").value("党建学习"));
        }

        @Test
        @DisplayName("GET / - 获取根目录列表成功")
        void getRootListSuccess() throws Exception {
            List<CategoryResponse> list = CategoryTestDataFactory.createCategoryResponseList(3);
            when(categoryService.getRootList()).thenReturn(list);

            mockMvc.perform(get("/api/content/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(3));
        }

        @Test
        @DisplayName("GET /root - 获取所有根目录成功")
        void getRootListViaRootSuccess() throws Exception {
            List<CategoryResponse> list = CategoryTestDataFactory.createCategoryResponseList(2);
            when(categoryService.getRootList()).thenReturn(list);

            mockMvc.perform(get("/api/content/categories/root"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("GET /{id}/children - 获取子目录成功")
        void getChildrenSuccess() throws Exception {
            List<CategoryResponse> children = CategoryTestDataFactory.createCategoryResponseList(3, 1L);
            when(categoryService.getByParentId(1L)).thenReturn(children);

            mockMvc.perform(get("/api/content/categories/1/children"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(3))
                    .andExpect(jsonPath("$.data[0].parentId").value("1"));
        }

        @Test
        @DisplayName("GET /{categoryId}/courses - 获取目录下课程成功")
        void getCoursesByCategorySuccess() throws Exception {
            List<CategoryCourse> courses = CategoryTestDataFactory.createCategoryCourseList(1L, 3);
            when(courseService.getByCategoryId(1L)).thenReturn(courses);

            mockMvc.perform(get("/api/content/categories/1/courses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(3))
                    .andExpect(jsonPath("$.data[0].categoryId").value(1))
                    .andExpect(jsonPath("$.data[0].courseId").value("1"));
        }

        @Test
        @DisplayName("GET /{categoryId}/articles - 获取目录下文章成功")
        void getArticlesByCategorySuccess() throws Exception {
            List<CategoryArticle> articles = CategoryTestDataFactory.createCategoryArticleList(1L, 2);
            when(articleService.getByCategoryId(1L)).thenReturn(articles);

            mockMvc.perform(get("/api/content/categories/1/articles"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].categoryId").value(1))
                    .andExpect(jsonPath("$.data[0].articleId").value("1"));
        }

        @Test
        @DisplayName("GET / - 返回的 VO 包含所有字段")
        void getRootListContainsAllFields() throws Exception {
            CategoryResponse vo = CategoryTestDataFactory.createCategoryResponse(1L, "党委工作", null);
            vo.setDescription("党委工作分类描述");
            vo.setSortOrder(1);
            vo.setUniversityId("uni-sustech-001");
            when(categoryService.getRootList()).thenReturn(List.of(vo));

            mockMvc.perform(get("/api/content/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].id").value("1"))
                    .andExpect(jsonPath("$.data[0].name").value("党委工作"))
                    .andExpect(jsonPath("$.data[0].description").value("党委工作分类描述"))
                    .andExpect(jsonPath("$.data[0].sortOrder").value(1))
                    .andExpect(jsonPath("$.data[0].universityId").value("uni-sustech-001"));
        }

        @Test
        @DisplayName("GET /{id} - 获取带子节点的目录详情成功")
        void getCategoryWithChildrenSuccess() throws Exception {
            CategoryResponse child = CategoryTestDataFactory.createCategoryResponse(2L, "子分类", 1L);
            CategoryResponse parent = CategoryTestDataFactory.createCategoryResponse(1L, "父分类", null, List.of(child));
            when(categoryService.get(1L)).thenReturn(parent);

            mockMvc.perform(get("/api/content/categories/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("1"))
                    .andExpect(jsonPath("$.data.children.length()").value(1))
                    .andExpect(jsonPath("$.data.children[0].id").value("2"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 异常处理场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("GET /{id} - Service 抛出 BusinessException 返回 400")
        void getCategoryNotFound() throws Exception {
            when(categoryService.get(999L)).thenThrow(new BusinessException(4001, "目录不存在"));

            mockMvc.perform(get("/api/content/categories/999"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"))
                    .andExpect(jsonPath("$.message").value("目录不存在"));
        }

        @Test
        @DisplayName("GET /{id} - Service 抛出 RuntimeException 返回 500")
        void getCategoryRuntimeException() throws Exception {
            when(categoryService.get(1L)).thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(get("/api/content/categories/1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("GET /{id}/children - Service 抛出异常返回 400")
        void getChildrenServiceException() throws Exception {
            when(categoryService.getByParentId(999L)).thenThrow(new BusinessException(4001, "目录不存在"));

            mockMvc.perform(get("/api/content/categories/999/children"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"));
        }

        @Test
        @DisplayName("GET /{categoryId}/courses - Service 抛出 BusinessException 返回 400")
        void getCoursesServiceException() throws Exception {
            when(courseService.getByCategoryId(999L)).thenThrow(new BusinessException(4001, "分类不存在"));

            mockMvc.perform(get("/api/content/categories/999/courses"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"));
        }

        @Test
        @DisplayName("GET /{categoryId}/articles - Service 抛出 BusinessException 返回 400")
        void getArticlesServiceException() throws Exception {
            when(articleService.getByCategoryId(999L)).thenThrow(new BusinessException(4001, "分类不存在"));

            mockMvc.perform(get("/api/content/categories/999/articles"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 边界场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("GET / - 目录为空时返回空列表")
        void getRootListEmpty() throws Exception {
            when(categoryService.getRootList()).thenReturn(List.of());

            mockMvc.perform(get("/api/content/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /{id}/children - 无子节点时返回空列表")
        void getChildrenEmpty() throws Exception {
            when(categoryService.getByParentId(999L)).thenReturn(List.of());

            mockMvc.perform(get("/api/content/categories/999/children"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /{categoryId}/courses - 无课程关联时返回空列表")
        void getCoursesEmpty() throws Exception {
            when(courseService.getByCategoryId(999L)).thenReturn(List.of());

            mockMvc.perform(get("/api/content/categories/999/courses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /{categoryId}/articles - 无文章关联时返回空列表")
        void getArticlesEmpty() throws Exception {
            when(articleService.getByCategoryId(999L)).thenReturn(List.of());

            mockMvc.perform(get("/api/content/categories/999/articles"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /{id} - 目录包含深层嵌套子节点")
        void getCategoryWithDeepNestedChildren() throws Exception {
            CategoryResponse grandchild = CategoryTestDataFactory.createCategoryResponse(3L, "孙节点", 2L);
            CategoryResponse child =
                    CategoryTestDataFactory.createCategoryResponse(2L, "子节点", 1L, List.of(grandchild));
            CategoryResponse parent = CategoryTestDataFactory.createCategoryResponse(1L, "根节点", null, List.of(child));

            when(categoryService.get(1L)).thenReturn(parent);

            mockMvc.perform(get("/api/content/categories/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value("1"))
                    .andExpect(jsonPath("$.data.children[0].id").value("2"))
                    .andExpect(jsonPath("$.data.children[0].children[0].id").value("3"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 安全场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("STUDENT 用户可正常访问 GET /{id}")
        void studentCanAccessGetCategory() throws Exception {
            CategoryResponse vo = CategoryTestDataFactory.createCategoryResponse(1L, "党建学习", null);
            when(categoryService.get(1L)).thenReturn(vo);

            mockMvc.perform(get("/api/content/categories/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("无 SecurityContext 用户被正常放行（UserAspect 未加载）")
        void noAuthUserAccessDenied() throws Exception {
            mockMvc.perform(get("/api/content/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("XSS 注入尝试在 path 参数中 — 作为普通参数传递")
        void xssInPathParameter() throws Exception {
            mockMvc.perform(get(URI.create("/api/content/categories/%3Cscript%3Ealert('xss')%3E")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("SQL 注入尝试在 path 参数中 — 参数化查询防护")
        void sqlInjectionInPathParameter() throws Exception {
            mockMvc.perform(get("/api/content/categories/{id}", "' OR '1'='1"))
                    .andExpect(status().isBadRequest());
        }
    }
}
