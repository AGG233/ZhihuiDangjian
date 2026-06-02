package com.rauio.smartdangjian.crosslayer.learning;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Field;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.graph.service.KnowledgeGraphService;
import com.rauio.smartdangjian.server.learning.constants.LearningErrorConstants;
import com.rauio.smartdangjian.server.learning.controller.user.UserLearningRecordController;
import com.rauio.smartdangjian.server.learning.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.server.learning.pojo.convertor.UserLearningRecordConvertor;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.learning.pojo.request.UserLearningRecordRequest;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;

@SpringBootTest(classes = UserLearningRecordControllerRealServiceIntegrationTest.TestConfig.class)
@DisplayName("学习记录控制层真实 UserLearningRecordService 集成测试")
class UserLearningRecordControllerRealServiceIntegrationTest extends CrossLayerTestBase {

    @Autowired
    private UserLearningRecordMapper recordMapper;

    @Autowired
    private UserLearningRecordConvertor convertor;

    @Autowired
    private KnowledgeGraphService knowledgeGraphService;

    @BeforeEach
    void resetMocks() {
        reset(recordMapper, convertor, knowledgeGraphService);
        setStudentContext(1L, "uni1");
    }

    @Test
    @DisplayName("GET /records/me/{id} 记录不存在时真实 Service 返回业务异常")
    void getMissingRecordReturnsBusinessException() throws Exception {
        when(recordMapper.selectById(404L)).thenReturn(null);

        mockMvc.perform(get("/api/learning/records/me/404"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(String.valueOf(LearningErrorConstants.RECORD_NOT_FOUND)))
                .andExpect(jsonPath("$.message").value("学习记录不存在"));
    }

    @Test
    @DisplayName("GET /records/me 使用真实 Service 查询并转换空列表")
    void getByUserIdUsesRealServiceAndConvertsEmptyList() throws Exception {
        when(recordMapper.selectList(any(Wrapper.class))).thenReturn(List.of());
        when(convertor.toResponseList(List.of())).thenReturn(List.of());

        mockMvc.perform(get("/api/learning/records/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.length()").value(0));

        verify(recordMapper).selectList(any(Wrapper.class));
        verify(convertor).toResponseList(List.of());
    }

    @Test
    @DisplayName("POST /records 使用真实 Service 自动计算 duration 并同步知识图谱")
    void createUsesRealServiceCalculatesDurationAndSyncsGraph() throws Exception {
        LocalDateTime start = LocalDateTime.of(2026, 5, 31, 10, 0);
        LocalDateTime end = LocalDateTime.of(2026, 5, 31, 10, 30);
        UserLearningRecord record = UserLearningRecord.builder()
                .userId(1L)
                .chapterId(10L)
                .startTime(start)
                .endTime(end)
                .deviceType("web")
                .build();
        when(convertor.toEntity(any(UserLearningRecordRequest.class))).thenReturn(record);
        when(recordMapper.insert(any(UserLearningRecord.class))).thenReturn(1);

        mockMvc.perform(post("/api/learning/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"chapterId\":10,\"startTime\":\"2026-05-31T10:00:00\","
                                + "\"endTime\":\"2026-05-31T10:30:00\",\"deviceType\":\"web\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));

        ArgumentCaptor<UserLearningRecord> captor = ArgumentCaptor.forClass(UserLearningRecord.class);
        verify(recordMapper).insert(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getDuration())
                .isEqualTo(1800);
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getCreatedAt())
                .isNotNull();
        verify(knowledgeGraphService).upsertLearningGraph(1L, 10L);
    }

    @Test
    @DisplayName("POST /records 保存失败时真实 Service 返回业务异常且不同步图谱")
    void createSaveFailureReturnsBusinessException() throws Exception {
        when(convertor.toEntity(any(UserLearningRecordRequest.class)))
                .thenReturn(
                        UserLearningRecord.builder().userId(1L).chapterId(10L).build());
        when(recordMapper.insert(any(UserLearningRecord.class))).thenReturn(0);

        mockMvc.perform(post("/api/learning/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"chapterId\":10,\"deviceType\":\"web\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(String.valueOf(LearningErrorConstants.RECORD_CREATE_FAILED)))
                .andExpect(jsonPath("$.message").value("创建学习记录失败"));

        verify(knowledgeGraphService, never()).upsertLearningGraph(any(), any());
    }

    @Test
    @DisplayName("PUT /records 缺少 id 时真实 Service 返回业务异常")
    void updateMissingIdReturnsBusinessException() throws Exception {
        mockMvc.perform(put("/api/learning/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"chapterId\":10,\"deviceType\":\"web\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(String.valueOf(LearningErrorConstants.RECORD_ID_REQUIRED)))
                .andExpect(jsonPath("$.message").value("更新时必须提供记录ID"));

        verify(recordMapper, never()).selectById(any());
        verify(recordMapper, never()).updateById(any(UserLearningRecord.class));
    }

    @Test
    @DisplayName("PUT /records 目标记录不存在时真实 Service 返回业务异常")
    void updateMissingRecordReturnsBusinessException() throws Exception {
        when(recordMapper.selectOne(any(Wrapper.class), org.mockito.ArgumentMatchers.anyBoolean()))
                .thenReturn(null);

        mockMvc.perform(put("/api/learning/records")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":99,\"userId\":1,\"chapterId\":10,\"deviceType\":\"web\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(String.valueOf(LearningErrorConstants.RECORD_NOT_FOUND)))
                .andExpect(jsonPath("$.message").value("学习记录不存在"));

        verify(recordMapper).selectOne(any(Wrapper.class), org.mockito.ArgumentMatchers.anyBoolean());
        verify(recordMapper, never()).selectById(any());
        verify(recordMapper, never()).updateById(any(UserLearningRecord.class));
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        UserLearningRecordMapper userLearningRecordMapper() {
            return mock(UserLearningRecordMapper.class);
        }

        @Bean
        UserLearningRecordConvertor userLearningRecordConvertor() {
            return mock(UserLearningRecordConvertor.class);
        }

        @Bean
        KnowledgeGraphService knowledgeGraphService() {
            return mock(KnowledgeGraphService.class);
        }

        @Bean
        @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
        UserLearningRecordService userLearningRecordService(
                UserLearningRecordConvertor convertor,
                KnowledgeGraphService knowledgeGraphService,
                UserLearningRecordMapper recordMapper) {
            UserLearningRecordService service =
                    new UserLearningRecordService(convertor, knowledgeGraphService, Clock.systemUTC());
            try {
                Field field = findBaseMapperField(service.getClass());
                field.setAccessible(true);
                field.set(service, recordMapper);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set baseMapper on UserLearningRecordService", e);
            }
            return service;
        }

        @Bean
        UserLearningRecordController userLearningRecordController(
                UserLearningRecordService recordService,
                com.rauio.smartdangjian.security.CurrentUserProvider currentUserProvider) {
            return new UserLearningRecordController(recordService, currentUserProvider);
        }

        @Bean
        AbstractPlatformTransactionManager transactionManager() {
            return new AbstractPlatformTransactionManager() {
                @Override
                protected Object doGetTransaction() {
                    return new Object();
                }

                @Override
                protected void doBegin(Object transaction, TransactionDefinition definition) {
                    // no-op
                }

                @Override
                protected void doCommit(DefaultTransactionStatus status) {
                    // no-op
                }

                @Override
                protected void doRollback(DefaultTransactionStatus status) {
                    // no-op
                }
            };
        }

        private static Field findBaseMapperField(Class<?> clazz) throws NoSuchFieldException {
            Class<?> current = clazz;
            while (current != null) {
                try {
                    return current.getDeclaredField("baseMapper");
                } catch (NoSuchFieldException e) {
                    current = current.getSuperclass();
                }
            }
            throw new NoSuchFieldException("baseMapper");
        }
    }
}
