package com.rauio.smartdangjian.controller.admin;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.content.constants.ArticleErrorConstants;
import com.rauio.smartdangjian.server.content.controller.admin.AdminArticleController;
import com.rauio.smartdangjian.server.content.pojo.request.ArticleRequest;
import com.rauio.smartdangjian.server.content.spec.ArticleStatus;
import com.rauio.smartdangjian.server.content.service.article.ArticleService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AdminArticleControllerTest.TestConfig.class)
@DisplayName("管理员文章接口测试")
class AdminArticleControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public AdminArticleController adminArticleController(ArticleService articleService) {
            return new AdminArticleController(articleService);
        }
    }

    @MockitoBean
    private ArticleService articleService;

    private String buildArticleJson(Long id, Long categoryId, String title) {
        return "{\"id\":" + (id == null ? "null" : id)
                + ",\"categoryId\":" + (categoryId == null ? "null" : categoryId)
                + ",\"title\":\"" + title + "\""
                + ",\"status\":\"" + ArticleStatus.Published.name() + "\"}";
    }

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("POST /api/admin/content/articles - 创建文章成功")
        void createArticleSuccess() throws Exception {
            doNothing().when(articleService).create(any(ArticleRequest.class));

            mockMvc.perform(post("/api/admin/content/articles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildArticleJson(null, 5L, "新文章")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));

            verify(articleService).create(any(ArticleRequest.class));
        }

        @Test
        @DisplayName("PUT /api/admin/content/articles/{id} - 更新文章成功")
        void updateArticleSuccess() throws Exception {
            doNothing().when(articleService).update(any(ArticleRequest.class));

            mockMvc.perform(put("/api/admin/content/articles/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildArticleJson(1L, 5L, "更新文章")))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));

            verify(articleService).update(any(ArticleRequest.class));
        }

        @Test
        @DisplayName("DELETE /api/admin/content/articles/{id} - 删除文章成功")
        void deleteArticleSuccess() throws Exception {
            doNothing().when(articleService).delete(1L);

            mockMvc.perform(delete("/api/admin/content/articles/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));

            verify(articleService).delete(1L);
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("创建文章 - Service 抛出 BusinessException 返回 400")
        void createArticleThrowsBusinessException() throws Exception {
            doThrow(new BusinessException(ArticleErrorConstants.ARTICLE_SAVE_FAILED, "文章保存失败"))
                    .when(articleService)
                    .create(any(ArticleRequest.class));

            mockMvc.perform(post("/api/admin/content/articles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildArticleJson(null, 5L, "失败文章")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(String.valueOf(ArticleErrorConstants.ARTICLE_SAVE_FAILED)))
                    .andExpect(jsonPath("$.message").value("文章保存失败"));
        }

        @Test
        @DisplayName("更新文章 - Service 抛出 BusinessException 返回 400")
        void updateArticleThrowsBusinessException() throws Exception {
            doThrow(new BusinessException(ArticleErrorConstants.ARTICLE_UPDATE_FAILED, "文章更新失败"))
                    .when(articleService)
                    .update(any(ArticleRequest.class));

            mockMvc.perform(put("/api/admin/content/articles/9999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildArticleJson(9999L, 5L, "失败文章")))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(String.valueOf(ArticleErrorConstants.ARTICLE_UPDATE_FAILED)))
                    .andExpect(jsonPath("$.message").value("文章更新失败"));
        }

        @Test
        @DisplayName("删除文章 - Service 抛出 BusinessException 返回 400")
        void deleteArticleThrowsBusinessException() throws Exception {
            doThrow(new BusinessException(ArticleErrorConstants.ARTICLE_DELETE_FAILED, "文章删除失败"))
                    .when(articleService)
                    .delete(anyLong());

            mockMvc.perform(delete("/api/admin/content/articles/9999"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(String.valueOf(ArticleErrorConstants.ARTICLE_DELETE_FAILED)))
                    .andExpect(jsonPath("$.message").value("文章删除失败"));
        }

        @Test
        @DisplayName("非法 JSON 请求体返回 400")
        void malformedJson() throws Exception {
            mockMvc.perform(post("/api/admin/content/articles")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("PUT - 文章 ID 非数字返回 400")
        void updateWithNonNumericId() throws Exception {
            mockMvc.perform(put("/api/admin/content/articles/文章")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(buildArticleJson(null, 5L, "文章")))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("DELETE - 文章 ID 非数字返回 400")
        void deleteWithNonNumericId() throws Exception {
            mockMvc.perform(delete("/api/admin/content/articles/文章"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("GET 请求创建文章接口返回 405")
        void createWithWrongMethod() throws Exception {
            mockMvc.perform(get("/api/admin/content/articles")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("GET 请求删除文章接口返回 405")
        void deleteWithWrongMethod() throws Exception {
            mockMvc.perform(get("/api/admin/content/articles/1")).andExpect(status().isMethodNotAllowed());
        }
    }
}
