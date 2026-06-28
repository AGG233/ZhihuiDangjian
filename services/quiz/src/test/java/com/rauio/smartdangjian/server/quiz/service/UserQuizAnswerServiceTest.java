package com.rauio.smartdangjian.server.quiz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.rauio.smartdangjian.server.quiz.mapper.UserQuizAnswerMapper;
import com.rauio.smartdangjian.server.quiz.pojo.dto.UserQuizAnswerSummaryDto;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;

@ExtendWith(MockitoExtension.class)
class UserQuizAnswerServiceTest {

    @BeforeAll
    static void initTableInfo() {
        MybatisConfiguration config = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(config, "");
        assistant.setCurrentNamespace(UserQuizAnswer.class.getName());
        TableInfoHelper.initTableInfo(assistant, UserQuizAnswer.class);
    }

    @Mock
    private UserQuizAnswerMapper mapper;

    @Spy
    @InjectMocks
    private UserQuizAnswerService userQuizAnswerService;

    @BeforeEach
    void resetSpy() {
        reset(userQuizAnswerService);
    }

    @Test
    @DisplayName("事务边界按方法声明：读方法只读，写方法显式回滚")
    void transactionalBoundariesAreMethodLevel() throws NoSuchMethodException {
        assertThat(UserQuizAnswerService.class.getAnnotation(Transactional.class))
                .isNull();
        assertReadOnlyTransaction("getByQuizId", Long.class);
        assertReadOnlyTransaction("getByOptionId", Long.class);
        assertReadOnlyTransaction("getByUserId", Long.class);
        assertReadOnlyTransaction("getByUserIdAndQuizId", Long.class, Long.class);
        assertReadOnlyTransaction("getByUserIdAndQuizIdAndOptionId", Long.class, Long.class, Long.class);
        assertWriteTransaction("create", UserQuizAnswer.class);
        assertWriteTransaction("update", UserQuizAnswer.class);
        assertWriteTransaction("updateByUserIdAndQuizIdAndOptionId", UserQuizAnswer.class);
        assertWriteTransaction("delete", Long.class);
        assertWriteTransaction("deleteByUserIdAndQuizIdAndOptionId", Long.class, Long.class, Long.class);
    }

    private void assertReadOnlyTransaction(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = UserQuizAnswerService.class.getMethod(methodName, parameterTypes);
        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.readOnly()).isTrue();
    }

    private void assertWriteTransaction(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = UserQuizAnswerService.class.getMethod(methodName, parameterTypes);
        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.readOnly()).isFalse();
        assertThat(transactional.rollbackFor()).contains(Exception.class);
    }

    // ==================== create ====================

    @Test
    @DisplayName("create 保存答题记录成功返回 true")
    void createReturnsTrueOnSuccess() {
        UserQuizAnswer answer =
                UserQuizAnswer.builder().userId(1L).quizId(1L).optionId(1L).build();
        doReturn(true).when(userQuizAnswerService).save(answer);

        Boolean result = userQuizAnswerService.create(answer);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("create 保存失败时返回 false")
    void createReturnsFalseOnFailure() {
        UserQuizAnswer answer = UserQuizAnswer.builder().build();
        doReturn(false).when(userQuizAnswerService).save(answer);

        Boolean result = userQuizAnswerService.create(answer);

        assertThat(result).isFalse();
    }

    // ==================== update ====================

    @Test
    @DisplayName("update 更新答题记录成功返回 true")
    void updateReturnsTrueOnSuccess() {
        UserQuizAnswer answer = UserQuizAnswer.builder().id(1L).scoreObtained(5).build();
        doReturn(true).when(userQuizAnswerService).updateById(answer);

        Boolean result = userQuizAnswerService.update(answer);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("update 更新失败时返回 false")
    void updateReturnsFalseOnFailure() {
        UserQuizAnswer answer = UserQuizAnswer.builder().id(1L).build();
        doReturn(false).when(userQuizAnswerService).updateById(answer);

        Boolean result = userQuizAnswerService.update(answer);

        assertThat(result).isFalse();
    }

    // ==================== updateByUserIdAndQuizIdAndOptionId ====================

    @Test
    @DisplayName("updateByUserIdAndQuizIdAndOptionId 记录存在时更新成功返回 true")
    void updateByCompositeKeyReturnsTrueWhenExistingFound() {
        UserQuizAnswer input = UserQuizAnswer.builder()
                .userId(1L)
                .quizId(1L)
                .optionId(1L)
                .scoreObtained(10)
                .build();
        UserQuizAnswer existing = UserQuizAnswer.builder()
                .id(1L)
                .userId(1L)
                .quizId(1L)
                .optionId(1L)
                .scoreObtained(5)
                .build();

        doReturn(existing).when(userQuizAnswerService).getByUserIdAndQuizIdAndOptionId(1L, 1L, 1L);
        // After setting id from existing, updateById is called with the input (now having id)
        doReturn(true).when(userQuizAnswerService).updateById(any(UserQuizAnswer.class));

        Boolean result = userQuizAnswerService.updateByUserIdAndQuizIdAndOptionId(input);

        assertThat(result).isTrue();
        assertThat(input.getId()).isEqualTo(1L);
        verify(userQuizAnswerService).updateById(input);
    }

    @Test
    @DisplayName("updateByUserIdAndQuizIdAndOptionId 记录不存在时返回 false")
    void updateByCompositeKeyReturnsFalseWhenNotFound() {
        UserQuizAnswer input =
                UserQuizAnswer.builder().userId(1L).quizId(1L).optionId(1L).build();

        doReturn(null).when(userQuizAnswerService).getByUserIdAndQuizIdAndOptionId(1L, 1L, 1L);

        Boolean result = userQuizAnswerService.updateByUserIdAndQuizIdAndOptionId(input);

        assertThat(result).isFalse();
        verify(userQuizAnswerService, never()).updateById(any(UserQuizAnswer.class));
    }

    // ==================== delete ====================

    @Test
    @DisplayName("delete 根据 ID 删除成功返回 true")
    void deleteReturnsTrueOnSuccess() {
        doReturn(true).when(userQuizAnswerService).removeById(1L);

        Boolean result = userQuizAnswerService.delete(1L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("delete 删除失败时返回 false")
    void deleteReturnsFalseOnFailure() {
        doReturn(false).when(userQuizAnswerService).removeById(1L);

        Boolean result = userQuizAnswerService.delete(1L);

        assertThat(result).isFalse();
    }

    // ==================== deleteByUserIdAndQuizIdAndOptionId ====================

    @Test
    @DisplayName("deleteByUserIdAndQuizIdAndOptionId 记录存在时删除成功返回 true")
    void deleteByCompositeKeyReturnsTrueWhenExistingFound() {
        UserQuizAnswer existing = UserQuizAnswer.builder()
                .id(1L)
                .userId(1L)
                .quizId(1L)
                .optionId(1L)
                .build();

        doReturn(existing).when(userQuizAnswerService).getByUserIdAndQuizIdAndOptionId(1L, 1L, 1L);
        doReturn(true).when(userQuizAnswerService).removeById(1L);

        Boolean result = userQuizAnswerService.deleteByUserIdAndQuizIdAndOptionId(1L, 1L, 1L);

        assertThat(result).isTrue();
        verify(userQuizAnswerService).removeById(1L);
    }

    @Test
    @DisplayName("deleteByUserIdAndQuizIdAndOptionId 记录不存在时返回 false")
    void deleteByCompositeKeyReturnsFalseWhenNotFound() {
        doReturn(null).when(userQuizAnswerService).getByUserIdAndQuizIdAndOptionId(1L, 1L, 1L);

        Boolean result = userQuizAnswerService.deleteByUserIdAndQuizIdAndOptionId(1L, 1L, 1L);

        assertThat(result).isFalse();
        verify(userQuizAnswerService, never()).removeById(anyLong());
    }

    // ==================== createForUser ====================

    @Nested
    @DisplayName("createForUser 为用户创建答题记录")
    class CreateForUser {

        @Test
        @DisplayName("创建答题记录成功")
        void createsForUserSuccessfully() {
            doReturn(true).when(userQuizAnswerService).save(any(UserQuizAnswer.class));

            Boolean result = userQuizAnswerService.createForUser(1L, 2L, 3L);

            assertThat(result).isTrue();
            ArgumentCaptor<UserQuizAnswer> captor = ArgumentCaptor.forClass(UserQuizAnswer.class);
            verify(userQuizAnswerService).save(captor.capture());
            assertThat(captor.getValue().getUserId()).isEqualTo(1L);
            assertThat(captor.getValue().getQuizId()).isEqualTo(2L);
            assertThat(captor.getValue().getOptionId()).isEqualTo(3L);
        }

        @Test
        @DisplayName("创建答题记录失败时返回 false")
        void createForUserReturnsFalseOnFailure() {
            doReturn(false).when(userQuizAnswerService).save(any(UserQuizAnswer.class));

            Boolean result = userQuizAnswerService.createForUser(1L, 2L, 3L);

            assertThat(result).isFalse();
        }
    }

    // ==================== listAnswerSummariesByUserId ====================

    @Nested
    @DisplayName("listAnswerSummariesByUserId 获取用户答题摘要")
    class ListAnswerSummariesByUserId {

        @Test
        @DisplayName("返回答案摘要 DTO 列表")
        void returnsSummaries() {
            UserQuizAnswer a1 = UserQuizAnswer.builder()
                    .userId(1L)
                    .quizId(10L)
                    .isCorrect(1)
                    .timeSpent(30)
                    .build();
            UserQuizAnswer a2 = UserQuizAnswer.builder()
                    .userId(1L)
                    .quizId(11L)
                    .isCorrect(0)
                    .timeSpent(45)
                    .build();
            doReturn(List.of(a1, a2)).when(userQuizAnswerService).list(any(LambdaQueryWrapper.class));

            List<UserQuizAnswerSummaryDto> result = userQuizAnswerService.listAnswerSummariesByUserId(1L);

            assertThat(result).hasSize(2);
            assertThat(result).allMatch(dto -> dto.userId().equals(1L));
        }

        @Test
        @DisplayName("用户无记录时返回空列表")
        void returnsEmptyWhenNoAnswers() {
            doReturn(Collections.emptyList()).when(userQuizAnswerService).list(any(LambdaQueryWrapper.class));

            List<UserQuizAnswerSummaryDto> result = userQuizAnswerService.listAnswerSummariesByUserId(1L);

            assertThat(result).isEmpty();
        }
    }

    // ==================== updateByUserIdAndQuizIdAndOptionId (triple param) ====================

    @Nested
    @DisplayName("updateByUserIdAndQuizIdAndOptionId(Long,Long,Long) 复合键更新")
    class UpdateByTripleParam {

        @Test
        @DisplayName("记录存在时构建并更新成功")
        void updatesWhenExistingFound() {
            UserQuizAnswer existing = UserQuizAnswer.builder()
                    .id(5L)
                    .userId(1L)
                    .quizId(2L)
                    .optionId(3L)
                    .build();
            doReturn(existing).when(userQuizAnswerService).getByUserIdAndQuizIdAndOptionId(1L, 2L, 3L);
            doReturn(true).when(userQuizAnswerService).updateById(any(UserQuizAnswer.class));

            Boolean result = userQuizAnswerService.updateByUserIdAndQuizIdAndOptionId(1L, 2L, 3L);

            assertThat(result).isTrue();
            ArgumentCaptor<UserQuizAnswer> captor = ArgumentCaptor.forClass(UserQuizAnswer.class);
            verify(userQuizAnswerService).updateById(captor.capture());
            assertThat(captor.getValue().getId()).isEqualTo(5L);
            assertThat(captor.getValue().getUserId()).isEqualTo(1L);
            assertThat(captor.getValue().getQuizId()).isEqualTo(2L);
            assertThat(captor.getValue().getOptionId()).isEqualTo(3L);
        }

        @Test
        @DisplayName("记录不存在时返回 false")
        void returnsFalseWhenNotFound() {
            doReturn(null).when(userQuizAnswerService).getByUserIdAndQuizIdAndOptionId(1L, 2L, 3L);

            Boolean result = userQuizAnswerService.updateByUserIdAndQuizIdAndOptionId(1L, 2L, 3L);

            assertThat(result).isFalse();
            verify(userQuizAnswerService, never()).updateById(any());
        }
    }

    // ==================== getByQuizId ====================

    @Test
    @DisplayName("getByQuizId 根据测验 ID 返回答题记录列表")
    void getByQuizIdReturnsAnswerList() {
        UserQuizAnswer a1 = UserQuizAnswer.builder().id(1L).quizId(1L).build();
        UserQuizAnswer a2 = UserQuizAnswer.builder().id(1L).quizId(1L).build();
        doReturn(List.of(a1, a2)).when(userQuizAnswerService).list(any(Wrapper.class));

        List<UserQuizAnswer> result = userQuizAnswerService.getByQuizId(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(UserQuizAnswer::getQuizId).containsOnly(1L);
    }

    @Test
    @DisplayName("getByQuizId 无记录时返回空列表")
    void getByQuizIdReturnsEmptyListWhenNoAnswers() {
        doReturn(Collections.emptyList()).when(userQuizAnswerService).list(any(Wrapper.class));

        List<UserQuizAnswer> result = userQuizAnswerService.getByQuizId(1L);

        assertThat(result).isEmpty();
    }

    // ==================== getByOptionId ====================

    @Test
    @DisplayName("getByOptionId 根据选项 ID 返回答题记录")
    void getByOptionIdReturnsAnswer() {
        UserQuizAnswer answer = UserQuizAnswer.builder().id(1L).optionId(1L).build();
        doReturn(answer).when(userQuizAnswerService).getOne(any(Wrapper.class));

        UserQuizAnswer result = userQuizAnswerService.getByOptionId(1L);

        assertThat(result).isNotNull();
        assertThat(result.getOptionId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getByOptionId 记录不存在时返回 null")
    void getByOptionIdReturnsNullWhenNotFound() {
        doReturn(null).when(userQuizAnswerService).getOne(any(Wrapper.class));

        UserQuizAnswer result = userQuizAnswerService.getByOptionId(1L);

        assertThat(result).isNull();
    }

    // ==================== getByUserId ====================

    @Test
    @DisplayName("getByUserId 根据用户 ID 返回答题记录列表")
    void getByUserIdReturnsAnswerList() {
        UserQuizAnswer a1 = UserQuizAnswer.builder().id(1L).userId(1L).build();
        doReturn(List.of(a1)).when(userQuizAnswerService).list(any(Wrapper.class));

        List<UserQuizAnswer> result = userQuizAnswerService.getByUserId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getByUserId 用户无答题记录时返回空列表")
    void getByUserIdReturnsEmptyListWhenNoAnswers() {
        doReturn(Collections.emptyList()).when(userQuizAnswerService).list(any(Wrapper.class));

        List<UserQuizAnswer> result = userQuizAnswerService.getByUserId(1L);

        assertThat(result).isEmpty();
    }

    // ==================== getByUserIdAndQuizId ====================

    @Test
    @DisplayName("getByUserIdAndQuizId 根据用户和测验 ID 返回答题记录列表")
    void getByUserIdAndQuizIdReturnsAnswerList() {
        UserQuizAnswer a1 =
                UserQuizAnswer.builder().id(1L).userId(1L).quizId(1L).build();
        doReturn(List.of(a1)).when(userQuizAnswerService).list(any(Wrapper.class));

        List<UserQuizAnswer> result = userQuizAnswerService.getByUserIdAndQuizId(1L, 1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(1L);
        assertThat(result.get(0).getQuizId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getByUserIdAndQuizId 无记录时返回空列表")
    void getByUserIdAndQuizIdReturnsEmptyListWhenNoAnswers() {
        doReturn(Collections.emptyList()).when(userQuizAnswerService).list(any(Wrapper.class));

        List<UserQuizAnswer> result = userQuizAnswerService.getByUserIdAndQuizId(1L, 1L);

        assertThat(result).isEmpty();
    }

    // ==================== getByUserIdAndQuizIdAndOptionId ====================

    @Test
    @DisplayName("getByUserIdAndQuizIdAndOptionId 根据用户、测验和选项 ID 返回答题记录")
    void getByThreeKeysReturnsAnswer() {
        UserQuizAnswer answer = UserQuizAnswer.builder()
                .id(1L)
                .userId(1L)
                .quizId(1L)
                .optionId(1L)
                .build();
        doReturn(answer).when(userQuizAnswerService).getOne(any(Wrapper.class));

        UserQuizAnswer result = userQuizAnswerService.getByUserIdAndQuizIdAndOptionId(1L, 1L, 1L);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);
        assertThat(result.getQuizId()).isEqualTo(1L);
        assertThat(result.getOptionId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getByUserIdAndQuizIdAndOptionId 记录不存在时返回 null")
    void getByThreeKeysReturnsNullWhenNotFound() {
        doReturn(null).when(userQuizAnswerService).getOne(any(Wrapper.class));

        UserQuizAnswer result = userQuizAnswerService.getByUserIdAndQuizIdAndOptionId(1L, 1L, 1L);

        assertThat(result).isNull();
    }
}
