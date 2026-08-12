package com.rauio.smartdangjian.controller.user;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.constants.ArticleErrorConstants;
import com.rauio.smartdangjian.server.content.controller.user.UserArticleController;
import com.rauio.smartdangjian.server.content.pojo.entity.Article;
import com.rauio.smartdangjian.server.content.pojo.response.ArticleResponse;
import com.rauio.smartdangjian.server.content.service.article.ArticleService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = UserArticleControllerTest.TestConfig.class)
@DisplayName("用户文章接口测试")
class UserArticleControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public UserArticleController userArticleController(ArticleService articleService) {
            return new UserArticleController(articleService);
        }
    }

    @MockitoBean
    private ArticleService articleService;

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("GET /api/content/articles/{id} - 获取文章详情成功（含内容块与分类）")
        void getArticleDetailSuccess() throws Exception {
            ArticleResponse response = ArticleResponse.builder()
                    .id(1L)
                    .title("文章标题")
                    .categoryId(5L)
                    .build();
            when(articleService.getDetail(1L)).thenReturn(response);

            mockMvc.perform(get("/api/content/articles/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("1"))
                    .andExpect(jsonPath("$.data.title").value("文章标题"))
                    .andExpect(jsonPath("$.data.categoryId").value("5"));
        }

        @Test
        @DisplayName("GET /api/content/articles - 分页获取文章列表成功")
        void getArticlesPageSuccess() throws Exception {
            Article a1 = Article.builder().id(1L).title("文章1").build();
            Article a2 = Article.builder().id(2L).title("文章2").build();
            when(articleService.getPage(1, 10)).thenReturn(List.of(a1, a2));

            mockMvc.perform(get("/api/content/articles"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].id").value("1"))
                    .andExpect(jsonPath("$.data[1].id").value("2"));
        }

        @Test
        @DisplayName("GET /api/content/articles/by-category/{categoryId} - 获取分类下文章成功")
        void getArticlesByCategorySuccess() throws Exception {
            Article a1 = Article.builder().id(1L).title("文章1").build();
            when(articleService.getArticlesByCategoryId(5L)).thenReturn(List.of(a1));

            mockMvc.perform(get("/api/content/articles/by-category/5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.length()").value(1))
                    .andExpect(jsonPath("$.data[0].id").value("1"));
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("GET /{id} - 文章不存在时 Service 抛出 BusinessException 返回 400")
        void getArticleNotFound() throws Exception {
            when(articleService.getDetail(1L))
                    .thenThrow(new BusinessException(ArticleErrorConstants.ARTICLE_NOT_FOUND, "文章不存在"));

            mockMvc.perform(get("/api/content/articles/1"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(String.valueOf(ArticleErrorConstants.ARTICLE_NOT_FOUND)))
                    .andExpect(jsonPath("$.message").value("文章不存在"));
        }

        @Test
        @DisplayName("GET /{id} - Service 抛出 RuntimeException 返回 500")
        void getArticleThrowsRuntimeException() throws Exception {
            when(articleService.getDetail(anyLong())).thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(get("/api/content/articles/1"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }
    }

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("GET /{id} - 非数字 ID 返回 400（Spring 类型转换失败）")
        void getWithNonNumericId() throws Exception {
            mockMvc.perform(get("/api/content/articles/文章"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("GET /by-category/{categoryId} - 分类下无文章返回空列表")
        void getByCategoryEmpty() throws Exception {
            when(articleService.getArticlesByCategoryId(999L)).thenReturn(List.of());

            mockMvc.perform(get("/api/content/articles/by-category/999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("GET /api/content/articles - 分页参数非法时返回 400")
        void getArticlesWithInvalidPaging() throws Exception {
            mockMvc.perform(get("/api/content/articles?pageNum=abc"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("POST 请求文章详情接口返回 405")
        void getDetailWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/content/articles/1")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("DELETE 请求文章列表接口返回 405")
        void getListWithWrongMethod() throws Exception {
            mockMvc.perform(delete("/api/content/articles")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("PUT 请求分类文章接口返回 405")
        void getByCategoryWithWrongMethod() throws Exception {
            mockMvc.perform(put("/api/content/articles/by-category/1"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }
}
