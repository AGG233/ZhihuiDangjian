package com.rauio.smartdangjian.server.quiz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.rauio.smartdangjian.server.quiz.mapper.UserQuizAnswerMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;

@ExtendWith(MockitoExtension.class)
class UserQuizAnswerServiceTest {

    @Mock
    private UserQuizAnswerMapper mapper;

    @Spy
    @InjectMocks
    private UserQuizAnswerService userQuizAnswerService;

    // ==================== create ====================

    @Test
    @DisplayName("create 保存答题记录成功返回 true")
    void createReturnsTrueOnSuccess() {
        UserQuizAnswer answer = UserQuizAnswer.builder()
                .userId(1L)
                .quizId(1L)
                .optionId(1L)
                .build();
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
        UserQuizAnswer answer =
                UserQuizAnswer.builder().id(1L).scoreObtained(5).build();
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
        UserQuizAnswer input = UserQuizAnswer.builder()
                .userId(1L)
                .quizId(1L)
                .optionId(1L)
                .build();

        doReturn(null).when(userQuizAnswerService).getByUserIdAndQuizIdAndOptionId(1L, 1L, 1L);

        Boolean result = userQuizAnswerService.updateByUserIdAndQuizIdAndOptionId(input);

        assertThat(result).isFalse();
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
        UserQuizAnswer answer =
                UserQuizAnswer.builder().id(1L).optionId(1L).build();
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
        UserQuizAnswer a1 = UserQuizAnswer.builder()
                .id(1L)
                .userId(1L)
                .quizId(1L)
                .build();
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
