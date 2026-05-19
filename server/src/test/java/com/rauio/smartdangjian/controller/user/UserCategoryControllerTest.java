package com.rauio.smartdangjian.controller.user;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.controller.factory.CategoryTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.server.content.controller.user.UserCategoryController;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryArticle;
import com.rauio.smartdangjian.server.content.pojo.entity.CategoryCourse;
import com.rauio.smartdangjian.server.content.pojo.vo.CategoryVO;
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
            public String getId() {
                return "stu-001";
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
        SecurityContextHolder.getContext()
                .setAuthentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        student, null, Collections.emptyList()));
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
            CategoryVO vo = CategoryTestDataFactory.createCategoryVO("cat-001", "党建学习", null);
            when(categoryService.get("cat-001")).thenReturn(vo);

            mockMvc.perform(get("/api/content/categories/cat-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("cat-001"))
                    .andExpect(jsonPath("$.data.name").value("党建学习"));
        }

        @Test
        @DisplayName("GET / - 获取根目录列表成功")
        void getRootListSuccess() throws Exception {
            List<CategoryVO> list = CategoryTestDataFactory.createCategoryVOList(3);
            when(categoryService.getRootList()).thenReturn(list);

            mockMvc.perform(get("/api/content/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(3));
        }

        @Test
        @DisplayName("GET /root - 获取所有根目录成功")
        void getRootListViaRootSuccess() throws Exception {
            List<CategoryVO> list = CategoryTestDataFactory.createCategoryVOList(2);
            when(categoryService.getRootList()).thenReturn(list);

            mockMvc.perform(get("/api/content/categories/root"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("GET /{id}/children - 获取子目录成功")
        void getChildrenSuccess() throws Exception {
            List<CategoryVO> children = CategoryTestDataFactory.createCategoryVOList(3, "cat-001");
            when(categoryService.getByParentId("cat-001")).thenReturn(children);

            mockMvc.perform(get("/api/content/categories/cat-001/children"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(3))
                    .andExpect(jsonPath("$.data[0].parentId").value("cat-001"));
        }

        @Test
        @DisplayName("GET /{categoryId}/courses - 获取目录下课程成功")
        void getCoursesByCategorySuccess() throws Exception {
            List<CategoryCourse> courses = CategoryTestDataFactory.createCategoryCourseList("cat-001", 3);
            when(courseService.getByCategoryId("cat-001")).thenReturn(courses);

            mockMvc.perform(get("/api/content/categories/cat-001/courses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(3))
                    .andExpect(jsonPath("$.data[0].categoryId").value("cat-001"))
                    .andExpect(jsonPath("$.data[0].courseId").value("course-001"));
        }

        @Test
        @DisplayName("GET /{categoryId}/articles - 获取目录下文章成功")
        void getArticlesByCategorySuccess() throws Exception {
            List<CategoryArticle> articles = CategoryTestDataFactory.createCategoryArticleList("cat-001", 2);
            when(articleService.getByCategoryId("cat-001")).thenReturn(articles);

            mockMvc.perform(get("/api/content/categories/cat-001/articles"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].categoryId").value("cat-001"))
                    .andExpect(jsonPath("$.data[0].articleId").value("article-001"));
        }

        @Test
        @DisplayName("GET / - 返回的 VO 包含所有字段")
        void getRootListContainsAllFields() throws Exception {
            CategoryVO vo = CategoryTestDataFactory.createCategoryVO("cat-001", "党委工作", null);
            vo.setDescription("党委工作分类描述");
            vo.setSortOrder(1);
            vo.setUniversityId("uni-sustech-001");
            when(categoryService.getRootList()).thenReturn(List.of(vo));

            mockMvc.perform(get("/api/content/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].id").value("cat-001"))
                    .andExpect(jsonPath("$.data[0].name").value("党委工作"))
                    .andExpect(jsonPath("$.data[0].description").value("党委工作分类描述"))
                    .andExpect(jsonPath("$.data[0].sortOrder").value(1))
                    .andExpect(jsonPath("$.data[0].universityId").value("uni-sustech-001"));
        }

        @Test
        @DisplayName("GET /{id} - 获取带子节点的目录详情成功")
        void getCategoryWithChildrenSuccess() throws Exception {
            CategoryVO child = CategoryTestDataFactory.createCategoryVO("child-001", "子分类", "cat-001");
            CategoryVO parent = CategoryTestDataFactory.createCategoryVO("cat-001", "父分类", null, List.of(child));
            when(categoryService.get("cat-001")).thenReturn(parent);

            mockMvc.perform(get("/api/content/categories/cat-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("cat-001"))
                    .andExpect(jsonPath("$.data.children.length()").value(1))
                    .andExpect(jsonPath("$.data.children[0].id").value("child-001"));
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
            when(categoryService.get("nonexistent")).thenThrow(new BusinessException(4001, "目录不存在"));

            mockMvc.perform(get("/api/content/categories/nonexistent"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"))
                    .andExpect(jsonPath("$.message").value("目录不存在"));
        }

        @Test
        @DisplayName("GET /{id} - Service 抛出 RuntimeException 返回 500")
        void getCategoryRuntimeException() throws Exception {
            when(categoryService.get("cat-001")).thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(get("/api/content/categories/cat-001"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("GET /{id}/children - Service 抛出异常返回 400")
        void getChildrenServiceException() throws Exception {
            when(categoryService.getByParentId("nonexistent")).thenThrow(new BusinessException(4001, "目录不存在"));

            mockMvc.perform(get("/api/content/categories/nonexistent/children"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"));
        }

        @Test
        @DisplayName("GET /{categoryId}/courses - Service 抛出 BusinessException 返回 400")
        void getCoursesServiceException() throws Exception {
            when(courseService.getByCategoryId("invalid")).thenThrow(new BusinessException(4001, "分类不存在"));

            mockMvc.perform(get("/api/content/categories/invalid/courses"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"));
        }

        @Test
        @DisplayName("GET /{categoryId}/articles - Service 抛出 BusinessException 返回 400")
        void getArticlesServiceException() throws Exception {
            when(articleService.getByCategoryId("invalid")).thenThrow(new BusinessException(4001, "分类不存在"));

            mockMvc.perform(get("/api/content/categories/invalid/articles"))
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
            when(categoryService.getByParentId("leaf-cat")).thenReturn(List.of());

            mockMvc.perform(get("/api/content/categories/leaf-cat/children"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /{categoryId}/courses - 无课程关联时返回空列表")
        void getCoursesEmpty() throws Exception {
            when(courseService.getByCategoryId("empty-cat")).thenReturn(List.of());

            mockMvc.perform(get("/api/content/categories/empty-cat/courses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /{categoryId}/articles - 无文章关联时返回空列表")
        void getArticlesEmpty() throws Exception {
            when(articleService.getByCategoryId("empty-cat")).thenReturn(List.of());

            mockMvc.perform(get("/api/content/categories/empty-cat/articles"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /{id} - 目录包含深层嵌套子节点")
        void getCategoryWithDeepNestedChildren() throws Exception {
            CategoryVO grandchild = CategoryTestDataFactory.createCategoryVO("grandchild-001", "孙节点", "child-001");
            CategoryVO child =
                    CategoryTestDataFactory.createCategoryVO("child-001", "子节点", "cat-001", List.of(grandchild));
            CategoryVO parent = CategoryTestDataFactory.createCategoryVO("cat-001", "根节点", null, List.of(child));

            when(categoryService.get("cat-001")).thenReturn(parent);

            mockMvc.perform(get("/api/content/categories/cat-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value("cat-001"))
                    .andExpect(jsonPath("$.data.children[0].id").value("child-001"))
                    .andExpect(jsonPath("$.data.children[0].children[0].id").value("grandchild-001"));
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
            CategoryVO vo = CategoryTestDataFactory.createCategoryVO("cat-001", "党建学习", null);
            when(categoryService.get("cat-001")).thenReturn(vo);

            mockMvc.perform(get("/api/content/categories/cat-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("无 SecurityContext 用户被正常放行（UserAspect 未加载）")
        void noAuthUserAccessDenied() throws Exception {
            SecurityContextHolder.clearContext();

            mockMvc.perform(get("/api/content/categories"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("XSS 注入尝试在 path 参数中 — 作为普通参数传递")
        void xssInPathParameter() throws Exception {
            when(categoryService.get("<script>alert('xss')</script>")).thenThrow(new BusinessException(4001, "目录不存在"));

            mockMvc.perform(get(URI.create("/api/content/categories/%3Cscript%3Ealert('xss')%3C%2Fscript%3E")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"));
        }

        @Test
        @DisplayName("SQL 注入尝试在 path 参数中 — 参数化查询防护")
        void sqlInjectionInPathParameter() throws Exception {
            when(categoryService.get("' OR '1'='1")).thenThrow(new BusinessException(4001, "目录不存在"));

            mockMvc.perform(get("/api/content/categories/{id}", "' OR '1'='1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"));
        }
    }
}
