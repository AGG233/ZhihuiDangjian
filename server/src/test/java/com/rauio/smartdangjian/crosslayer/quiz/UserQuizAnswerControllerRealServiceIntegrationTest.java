package com.rauio.smartdangjian.crosslayer.quiz;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Field;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.quiz.controller.admin.AdminQuizAnswerController;
import com.rauio.smartdangjian.server.quiz.controller.user.UserQuizAnswerController;
import com.rauio.smartdangjian.server.quiz.mapper.UserQuizAnswerMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;
import com.rauio.smartdangjian.server.quiz.service.UserQuizAnswerService;
import com.rauio.smartdangjian.utils.spec.UserType;

@SpringBootTest(classes = UserQuizAnswerControllerRealServiceIntegrationTest.TestConfig.class)
@DisplayName("答题记录控制层真实 UserQuizAnswerService 集成测试")
class UserQuizAnswerControllerRealServiceIntegrationTest extends CrossLayerTestBase {

    @Autowired
    private UserQuizAnswerMapper answerMapper;

    @BeforeEach
    void resetMocks() {
        reset(answerMapper);
        setSecurityContext(UserType.STUDENT, 1L, "uni1");
    }

    @Test
    @DisplayName("GET /answers/me 使用真实 UserQuizAnswerService 查询当前用户列表")
    void getByUserIdUsesRealService() throws Exception {
        when(answerMapper.selectList(any(Wrapper.class)))
                .thenReturn(List.of(answer(100L, 1L, 10L, 20L), answer(101L, 1L, 11L, 21L)));

        mockMvc.perform(get("/api/quiz/answers/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value("100"))
                .andExpect(jsonPath("$.data[1].optionId").value("21"));

        verify(answerMapper).selectList(any(Wrapper.class));
    }

    @Test
    @DisplayName("GET /answers/users/{id} 旧他人答题记录路由已移除")
    void oldUserScopedRouteReturns404BeforeService() throws Exception {
        setSecurityContext(UserType.STUDENT, 1L, "uni1");

        mockMvc.perform(get("/api/quiz/answers/users/2")).andExpect(status().isNotFound());

        verify(answerMapper, never()).selectList(any(Wrapper.class));
    }

    @Test
    @DisplayName("POST /answers/me/quizzes/{quizId}/options/{optionId} 使用真实 Service 创建答题记录")
    void createUsesRealServiceAndSetsPathFields() throws Exception {
        when(answerMapper.insert(any(UserQuizAnswer.class))).thenReturn(1);

        mockMvc.perform(post("/api/quiz/answers/me/quizzes/10/options/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));

        ArgumentCaptor<UserQuizAnswer> captor = ArgumentCaptor.forClass(UserQuizAnswer.class);
        verify(answerMapper).insert(captor.capture());
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getUserId())
                .isEqualTo(1L);
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getQuizId())
                .isEqualTo(10L);
        org.assertj.core.api.Assertions.assertThat(captor.getValue().getOptionId())
                .isEqualTo(20L);
    }

    @Test
    @DisplayName("PUT /answers/me/quizzes/{quizId}/options/{optionId} 无现有记录时真实 Service 返回 false")
    void updateMissingExistingAnswerReturnsFalse() throws Exception {
        when(answerMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(null);

        mockMvc.perform(put("/api/quiz/answers/me/quizzes/10/options/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(false));

        verify(answerMapper).selectOne(any(Wrapper.class), anyBoolean());
        verify(answerMapper, never()).updateById(any(UserQuizAnswer.class));
    }

    @Test
    @DisplayName("DELETE /admin/quiz/answers/... 无现有记录时真实 Service 返回 false")
    void adminDeleteMissingExistingAnswerReturnsFalse() throws Exception {
        setSecurityContext(UserType.MANAGER, 1L, "uni1");
        when(answerMapper.selectOne(any(Wrapper.class), anyBoolean())).thenReturn(null);

        mockMvc.perform(delete("/api/admin/quiz/answers/users/1/quizzes/10/options/20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(false));

        verify(answerMapper).selectOne(any(Wrapper.class), anyBoolean());
        verify(answerMapper, never()).deleteById(any(Long.class));
    }

    private static UserQuizAnswer answer(Long id, Long userId, Long quizId, Long optionId) {
        return UserQuizAnswer.builder()
                .id(id)
                .userId(userId)
                .quizId(quizId)
                .optionId(optionId)
                .isCorrect(1)
                .scoreObtained(5)
                .build();
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        UserQuizAnswerMapper userQuizAnswerMapper() {
            return mock(UserQuizAnswerMapper.class);
        }

        @Bean
        @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
        UserQuizAnswerService userQuizAnswerService(UserQuizAnswerMapper answerMapper) {
            UserQuizAnswerService service = new UserQuizAnswerService();
            try {
                Field field = findBaseMapperField(service.getClass());
                field.setAccessible(true);
                field.set(service, answerMapper);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set baseMapper on UserQuizAnswerService", e);
            }
            return service;
        }

        @Bean
        UserQuizAnswerController userQuizAnswerController(
                UserQuizAnswerService answerService,
                com.rauio.smartdangjian.security.CurrentUserProvider currentUserProvider) {
            return new UserQuizAnswerController(answerService, currentUserProvider);
        }

        @Bean
        AdminQuizAnswerController adminQuizAnswerController(UserQuizAnswerService answerService) {
            return new AdminQuizAnswerController(answerService);
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
