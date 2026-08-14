package com.rauio.smartdangjian.controller.admin;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import com.rauio.smartdangjian.server.ai.constants.AiErrorConstants;
import com.rauio.smartdangjian.server.ai.rag.DocumentIngestionController;
import com.rauio.smartdangjian.server.ai.rag.DocumentIngestionService;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = DocumentIngestionControllerTest.TestConfig.class)
@DisplayName("RAG文档入库接口测试")
class DocumentIngestionControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public DocumentIngestionController documentIngestionController(
                DocumentIngestionService documentIngestionService) {
            return new DocumentIngestionController(documentIngestionService);
        }
    }

    @MockitoBean
    private DocumentIngestionService documentIngestionService;

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("POST /ingest - 全量入库成功")
        void ingestAllSuccess() throws Exception {
            when(documentIngestionService.ingestAll()).thenReturn(42);

            mockMvc.perform(post("/api/ai/rag/documents/ingest").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(42));
        }

        @Test
        @DisplayName("POST /ingest/{type}/{id} - 文章增量入库成功")
        void ingestArticleByIdSuccess() throws Exception {
            when(documentIngestionService.ingestById("article", "1")).thenReturn(3);

            mockMvc.perform(post("/api/ai/rag/documents/ingest/article/1").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(3));
        }

        @Test
        @DisplayName("POST /ingest/{type}/{id} - 章节增量入库成功")
        void ingestChapterByIdSuccess() throws Exception {
            when(documentIngestionService.ingestById("chapter", "10")).thenReturn(2);

            mockMvc.perform(post("/api/ai/rag/documents/ingest/chapter/10").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(2));
        }
    }

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("Service 抛出 BusinessException 返回 400")
        void serviceThrowsBusinessException() throws Exception {
            when(documentIngestionService.ingestById(anyString(), anyString()))
                    .thenThrow(new BusinessException(AiErrorConstants.DOCUMENT_TYPE_INVALID, "不支持的文档类型: course"));

            mockMvc.perform(post("/api/ai/rag/documents/ingest/course/1").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(String.valueOf(AiErrorConstants.DOCUMENT_TYPE_INVALID)))
                    .andExpect(jsonPath("$.message").value("不支持的文档类型: course"));
        }

        @Test
        @DisplayName("Service 抛出 RuntimeException 返回 500")
        void serviceThrowsRuntimeException() throws Exception {
            when(documentIngestionService.ingestById(anyString(), anyString()))
                    .thenThrow(new RuntimeException("数据库异常"));

            mockMvc.perform(post("/api/ai/rag/documents/ingest/article/1").accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }
    }

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("GET 请求全量入库接口返回 405")
        void ingestAllWithWrongMethod() throws Exception {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get(
                            "/api/ai/rag/documents/ingest"))
                    .andExpect(status().isMethodNotAllowed());
        }
    }
}
