package com.rauio.smartdangjian.crosslayer.learning;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
import com.rauio.smartdangjian.server.learning.controller.admin.AdminLearningRecordController;
import com.rauio.smartdangjian.server.learning.pojo.response.UserLearningRecordResponse;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;

@SpringBootTest(classes = AdminLearningRecordControllerRealServiceIntegrationTest.TestConfig.class)
@DisplayName("管理员学习记录控制层集成测试")
class AdminLearningRecordControllerRealServiceIntegrationTest extends CrossLayerTestBase {

    @MockitoBean
    private UserLearningRecordService recordService;

    @BeforeEach
    void setUp() {
        reset(recordService);
        setSchoolContext(1L, "uni-1");
    }

    @Test
    @DisplayName("GET /api/admin/learning/records/chapter/{chapterId} 成功返回章节学习记录")
    void getByChapterId() throws Exception {
        UserLearningRecordResponse record = UserLearningRecordResponse.builder()
                .id(100L)
                .chapterId(10L)
                .userId(1L)
                .build();
        when(recordService.getByChapterId(10L)).thenReturn(List.of(record));

        mockMvc.perform(get("/api/admin/learning/records/chapter/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].id").value(100))
                .andExpect(jsonPath("$.data[0].chapterId").value(10));

        verify(recordService).getByChapterId(10L);
    }

    @Test
    @DisplayName("DELETE /api/admin/learning/records/{id} 成功删除学习记录")
    void deleteRecord() throws Exception {
        when(recordService.delete(100L)).thenReturn(true);

        mockMvc.perform(delete("/api/admin/learning/records/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));

        verify(recordService).delete(100L);
    }

    @Test
    @DisplayName("GET /api/admin/learning/records/chapter/{chapterId} 无记录时返回空列表")
    void getByChapterIdEmpty() throws Exception {
        when(recordService.getByChapterId(999L)).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/learning/records/chapter/999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        AdminLearningRecordController adminLearningRecordController(UserLearningRecordService recordService) {
            return new AdminLearningRecordController(recordService);
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
