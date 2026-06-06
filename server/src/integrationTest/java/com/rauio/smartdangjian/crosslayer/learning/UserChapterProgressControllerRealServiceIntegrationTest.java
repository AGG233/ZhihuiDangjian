package com.rauio.smartdangjian.crosslayer.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
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

import java.time.Clock;
import java.util.Date;
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
import com.rauio.smartdangjian.server.learning.constants.LearningErrorConstants;
import com.rauio.smartdangjian.server.learning.controller.user.UserChapterProgressController;
import com.rauio.smartdangjian.server.learning.mapper.UserChapterProgressMapper;
import com.rauio.smartdangjian.server.learning.pojo.convertor.UserChapterProgressConvertor;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserChapterProgress;
import com.rauio.smartdangjian.server.learning.pojo.request.UserChapterProgressRequest;
import com.rauio.smartdangjian.server.learning.pojo.response.UserChapterProgressResponse;
import com.rauio.smartdangjian.server.learning.service.UserChapterProgressService;

@SpringBootTest(classes = UserChapterProgressControllerRealServiceIntegrationTest.TestConfig.class)
@DisplayName("用户章节进度控制层真实 UserChapterProgressService 集成测试")
class UserChapterProgressControllerRealServiceIntegrationTest extends CrossLayerTestBase {

    @Autowired
    private UserChapterProgressMapper progressMapper;

    @Autowired
    private UserChapterProgressConvertor convertor;

    @BeforeEach
    void resetMocks() {
        reset(progressMapper, convertor);
        setStudentContext(1L, "uni1");
    }

    @Test
    @DisplayName("POST /progress 使用真实 UserChapterProgressService 创建进度并补齐时间")
    void createUsesRealProgressServiceAndSetsFirstViewedAt() throws Exception {
        UserChapterProgress progress = UserChapterProgress.builder()
                .userId(1L)
                .chapterId(10L)
                .progress(50)
                .status("in_progress")
                .build();
        when(progressMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(null);
        when(convertor.toEntity(any(UserChapterProgressRequest.class))).thenReturn(progress);
        when(progressMapper.insert(any(UserChapterProgress.class))).thenReturn(1);

        mockMvc.perform(post("/api/learning/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"chapterId\":10,\"progress\":50,\"status\":\"in_progress\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));

        ArgumentCaptor<UserChapterProgress> captor = ArgumentCaptor.forClass(UserChapterProgress.class);
        verify(progressMapper).insert(captor.capture());
        assertThat(captor.getValue().getUpdatedAt()).isNotNull();
        assertThat(captor.getValue().getFirstViewedAt()).isNotNull();
    }

    @Test
    @DisplayName("POST /progress progress 大于100时字段校验短路")
    void createRejectsProgressAboveMaxBeforeServiceDependencies() throws Exception {
        mockMvc.perform(post("/api/learning/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"chapterId\":10,\"progress\":101,\"status\":\"in_progress\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("学习进度不能大于100"));

        verify(progressMapper, never()).selectOne(any(Wrapper.class), anyBoolean());
        verify(progressMapper, never()).insert(any(UserChapterProgress.class));
    }

    @Test
    @DisplayName("POST /progress 重复记录由真实 UserChapterProgressService 返回业务异常")
    void createDuplicateProgressReturnsBusinessException() throws Exception {
        when(progressMapper.selectOne(any(Wrapper.class), anyBoolean()))
                .thenReturn(UserChapterProgress.builder().id(99L).build());

        mockMvc.perform(post("/api/learning/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"chapterId\":10,\"progress\":50,\"status\":\"in_progress\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(String.valueOf(LearningErrorConstants.PROGRESS_ALREADY_EXISTS)))
                .andExpect(jsonPath("$.message").value("该用户的章节进度记录已存在"));

        verify(progressMapper, never()).insert(any(UserChapterProgress.class));
    }

    @Test
    @DisplayName("PUT /progress 使用真实 UserChapterProgressService 完成进度时设置 completed 状态")
    void updateUsesRealProgressServiceAndMarksCompleted() throws Exception {
        UserChapterProgress existing = UserChapterProgress.builder()
                .id(100L)
                .userId(1L)
                .chapterId(10L)
                .progress(80)
                .status("in_progress")
                .build();
        UserChapterProgress updated = UserChapterProgress.builder()
                .id(100L)
                .userId(1L)
                .chapterId(10L)
                .progress(100)
                .status("in_progress")
                .build();

        when(progressMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(existing);
        when(progressMapper.selectById(100L)).thenReturn(existing);
        when(convertor.toEntity(any(UserChapterProgressRequest.class))).thenReturn(updated);
        when(progressMapper.updateById(any(UserChapterProgress.class))).thenReturn(1);

        mockMvc.perform(
                        put("/api/learning/progress")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"id\":100,\"userId\":1,\"chapterId\":10,\"progress\":100,\"status\":\"in_progress\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));

        ArgumentCaptor<UserChapterProgress> captor = ArgumentCaptor.forClass(UserChapterProgress.class);
        verify(progressMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo("completed");
        assertThat(captor.getValue().getCompletedAt()).isNotNull();
        assertThat(captor.getValue().getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("PUT /progress 缺少 id 时由真实 UserChapterProgressService 返回业务异常")
    void updateMissingIdReturnsBusinessException() throws Exception {
        mockMvc.perform(put("/api/learning/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":1,\"chapterId\":10,\"progress\":90,\"status\":\"in_progress\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(String.valueOf(LearningErrorConstants.PROGRESS_ID_REQUIRED)))
                .andExpect(jsonPath("$.message").value("更新时必须提供进度ID"));

        verify(progressMapper, never()).selectById(any());
        verify(progressMapper, never()).updateById(any(UserChapterProgress.class));
    }

    @Test
    @DisplayName("GET /progress/me 使用真实 UserChapterProgressService 查询并转换列表")
    void getByUserIdUsesRealProgressService() throws Exception {
        List<UserChapterProgress> records = List.of(UserChapterProgress.builder()
                .id(100L)
                .userId(1L)
                .chapterId(10L)
                .progress(50)
                .build());
        List<UserChapterProgressResponse> responses = List.of(UserChapterProgressResponse.builder()
                .id(100L)
                .userId(1L)
                .chapterId(10L)
                .progress(50)
                .updatedAt(new Date())
                .build());
        when(progressMapper.selectList(any(Wrapper.class))).thenReturn(records);
        when(convertor.toResponseList(records)).thenReturn(responses);

        mockMvc.perform(get("/api/learning/progress/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].id").value(100))
                .andExpect(jsonPath("$.data[0].progress").value(50));

        verify(progressMapper).selectList(any(Wrapper.class));
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        UserChapterProgressMapper userChapterProgressMapper() {
            return mock(UserChapterProgressMapper.class);
        }

        @Bean
        UserChapterProgressConvertor userChapterProgressConvertor() {
            return mock(UserChapterProgressConvertor.class);
        }

        @Bean
        UserChapterProgressService userChapterProgressService(
                UserChapterProgressConvertor convertor, UserChapterProgressMapper progressMapper) {
            UserChapterProgressService service = new UserChapterProgressService(convertor, Clock.systemUTC());
            try {
                org.springframework.test.util.ReflectionTestUtils.setField(service, "baseMapper", progressMapper);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set baseMapper on UserChapterProgressService", e);
            }
            return service;
        }

        @Bean
        UserChapterProgressController userChapterProgressController(
                UserChapterProgressService progressService,
                com.rauio.smartdangjian.security.CurrentUserProvider currentUserProvider) {
            return new UserChapterProgressController(progressService, currentUserProvider);
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
    }
}
