package com.rauio.smartdangjian.crosslayer.chapter;

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
import com.rauio.smartdangjian.server.chapter.controller.user.UserChapterController;
import com.rauio.smartdangjian.server.chapter.pojo.response.ChapterResponse;
import com.rauio.smartdangjian.server.chapter.service.chapter.ChapterService;

@SpringBootTest(classes = UserChapterControllerRealServiceIntegrationTest.TestConfig.class)
@DisplayName("用户章节控制层集成测试")
class UserChapterControllerRealServiceIntegrationTest extends CrossLayerTestBase {

    @MockitoBean
    private ChapterService chapterService;

    @BeforeEach
    void setUp() {
        reset(chapterService);
        setStudentContext(1L, "uni-1");
    }

    @Test
    @DisplayName("GET /api/content/chapters/{id} 成功返回章节详情")
    void getChapterById() throws Exception {
        ChapterResponse response =
                ChapterResponse.builder().id(100L).courseId(1L).title("第一章").build();
        when(chapterService.get(100L)).thenReturn(response);

        mockMvc.perform(get("/api/content/chapters/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.title").value("第一章"));

        verify(chapterService).get(100L);
    }

    @Test
    @DisplayName("GET /api/content/chapters/by-course/{courseId} 成功返回章节列表")
    void getByCourseId() throws Exception {
        ChapterResponse chapter1 =
                ChapterResponse.builder().id(100L).courseId(1L).title("第一章").build();
        ChapterResponse chapter2 =
                ChapterResponse.builder().id(101L).courseId(1L).title("第二章").build();
        when(chapterService.getByCourseId(1L)).thenReturn(List.of(chapter1, chapter2));

        mockMvc.perform(get("/api/content/chapters/by-course/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].title").value("第一章"))
                .andExpect(jsonPath("$.data[1].title").value("第二章"));

        verify(chapterService).getByCourseId(1L);
    }

    @Test
    @DisplayName("GET /api/content/chapters/{id} 章节不存在时返回业务异常")
    void getChapterNotFound() throws Exception {
        when(chapterService.get(999L))
                .thenThrow(new com.rauio.smartdangjian.exception.BusinessException(
                        com.rauio.smartdangjian.server.chapter.constants.ChapterErrorConstants.CHAPTER_NOT_FOUND,
                        "章节不存在"));

        mockMvc.perform(get("/api/content/chapters/999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value(String.valueOf(
                                com.rauio.smartdangjian.server.chapter.constants.ChapterErrorConstants
                                        .CHAPTER_NOT_FOUND)))
                .andExpect(jsonPath("$.message").value("章节不存在"));
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        UserChapterController userChapterController(ChapterService chapterService) {
            return new UserChapterController(chapterService);
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
