package com.rauio.smartdangjian.crosslayer.learning;

import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.server.learning.controller.user.UserLearningGraphSyncController;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;

@SpringBootTest(classes = UserLearningGraphSyncControllerRealServiceIntegrationTest.TestConfig.class)
@DisplayName("学习图谱同步控制层集成测试")
class UserLearningGraphSyncControllerRealServiceIntegrationTest extends CrossLayerTestBase {

    @MockitoBean
    private UserLearningRecordService userLearningRecordService;

    @BeforeEach
    void setUp() {
        reset(userLearningRecordService);
        setStudentContext(1L, "uni-1");
    }

    @Test
    @DisplayName("POST /api/learning/graph/me/sync 成功同步用户学习图谱")
    void syncMyGraph() throws Exception {
        when(userLearningRecordService.syncUserLearningGraph(1L)).thenReturn(5);

        mockMvc.perform(post("/api/learning/graph/me/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(5));

        verify(userLearningRecordService).syncUserLearningGraph(1L);
    }

    @Test
    @DisplayName("POST /api/learning/graph/me/sync 无未同步记录时返回0")
    void syncMyGraphNoRecords() throws Exception {
        when(userLearningRecordService.syncUserLearningGraph(1L)).thenReturn(0);

        mockMvc.perform(post("/api/learning/graph/me/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(0));
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        UserLearningGraphSyncController userLearningGraphSyncController(
                UserLearningRecordService userLearningRecordService, CurrentUserProvider currentUserProvider) {
            return new UserLearningGraphSyncController(userLearningRecordService, currentUserProvider);
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
