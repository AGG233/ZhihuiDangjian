package com.rauio.smartdangjian.server.quiz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;

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

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.quiz.constants.QuizErrorConstants;
import com.rauio.smartdangjian.server.quiz.mapper.QuizOptionMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;
import com.rauio.smartdangjian.server.quiz.pojo.request.QuizOptionRequest;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.utils.spec.UserType;

@ExtendWith(MockitoExtension.class)
class QuizOptionServiceTest {

    @Mock
    private QuizOptionMapper mapper;

    @Mock
    private UserQuizAnswerService userQuizAnswerService;

    @Mock
    private UserService userService;

    @Spy
    @InjectMocks
    private QuizOptionService quizOptionService;

    @BeforeEach
    void resetSpy() {
        reset(quizOptionService);
    }

    @Test
    @DisplayName("事务边界按方法声明：读方法只读，写方法显式回滚")
    void transactionalBoundariesAreMethodLevel() throws NoSuchMethodException {
        assertThat(QuizOptionService.class.getAnnotation(Transactional.class)).isNull();
        assertReadOnlyTransaction("getByQuizId", Long.class);
        assertReadOnlyTransaction("get", Long.class);
        assertWriteTransaction("create", Long.class, QuizOption.class);
        assertWriteTransaction("update", Long.class, QuizOption.class);
        assertWriteTransaction("delete", Long.class);
    }

    private void assertReadOnlyTransaction(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = QuizOptionService.class.getMethod(methodName, parameterTypes);
        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.readOnly()).isTrue();
    }

    private void assertWriteTransaction(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = QuizOptionService.class.getMethod(methodName, parameterTypes);
        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.readOnly()).isFalse();
        assertThat(transactional.rollbackFor()).contains(Exception.class);
    }

    // ==================== update ====================

    @Test
    @DisplayName("update 设置选项 ID 后调用 updateById 成功返回 true")
    void updateSetsIdAndReturnsTrueOnSuccess() {
        QuizOption option =
                QuizOption.builder().optionText("选项A").isCorrect(true).build();
        doReturn(true).when(quizOptionService).updateById(option);

        Boolean result = quizOptionService.update(1L, option);

        assertThat(result).isTrue();
        assertThat(option.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("update 更新失败时返回 false")
    void updateReturnsFalseOnFailure() {
        QuizOption option = QuizOption.builder().optionText("选项A").build();
        doReturn(false).when(quizOptionService).updateById(option);

        Boolean result = quizOptionService.update(1L, option);

        assertThat(result).isFalse();
    }

    // ==================== create ====================

    @Test
    @DisplayName("create 设置 quizId 后调用 save 成功返回 true")
    void createSetsQuizIdAndReturnsTrueOnSuccess() {
        QuizOption option =
                QuizOption.builder().optionText("新选项").isCorrect(false).build();
        doReturn(true).when(quizOptionService).save(option);

        Boolean result = quizOptionService.create(1L, option);

        assertThat(result).isTrue();
        assertThat(option.getQuizId()).isEqualTo(1L);
        ArgumentCaptor<QuizOption> optionCaptor = ArgumentCaptor.forClass(QuizOption.class);
        verify(quizOptionService).save(optionCaptor.capture());
        assertThat(optionCaptor.getValue().getQuizId()).isEqualTo(1L);
        assertThat(optionCaptor.getValue().getOptionText()).isEqualTo("新选项");
    }

    @Test
    @DisplayName("create 保存失败时返回 false")
    void createReturnsFalseOnFailure() {
        QuizOption option = QuizOption.builder().optionText("新选项").build();
        doReturn(false).when(quizOptionService).save(option);

        Boolean result = quizOptionService.create(1L, option);

        assertThat(result).isFalse();
    }

    // ==================== update (overloaded) ====================

    @Nested
    @DisplayName("update(Long, QuizOptionRequest) 重载方法")
    class UpdateFromRequest {

        @Test
        @DisplayName("从请求对象更新选项成功")
        void updatesFromRequestSuccessfully() {
            QuizOptionRequest request = new QuizOptionRequest("新选项", true, "A");
            doReturn(true).when(quizOptionService).updateById(any(QuizOption.class));

            Boolean result = quizOptionService.update(1L, request);

            assertThat(result).isTrue();
            ArgumentCaptor<QuizOption> captor = ArgumentCaptor.forClass(QuizOption.class);
            verify(quizOptionService).updateById(captor.capture());
            assertThat(captor.getValue().getId()).isEqualTo(1L);
            assertThat(captor.getValue().getOptionText()).isEqualTo("新选项");
            assertThat(captor.getValue().getIsCorrect()).isTrue();
            assertThat(captor.getValue().getOrderIndex()).isEqualTo("A");
        }

        @Test
        @DisplayName("从请求对象更新失败时返回 false")
        void updateFromRequestReturnsFalseOnFailure() {
            QuizOptionRequest request = new QuizOptionRequest("新选项", true, "A");
            doReturn(false).when(quizOptionService).updateById(any(QuizOption.class));

            Boolean result = quizOptionService.update(1L, request);

            assertThat(result).isFalse();
        }
    }

    // ==================== create (overloaded) ====================

    @Nested
    @DisplayName("create(Long, QuizOptionRequest) 重载方法")
    class CreateFromRequest {

        @Test
        @DisplayName("从请求对象创建选项成功")
        void createsFromRequestSuccessfully() {
            QuizOptionRequest request = new QuizOptionRequest("新选项", false, "B");
            doReturn(true).when(quizOptionService).save(any(QuizOption.class));

            Boolean result = quizOptionService.create(1L, request);

            assertThat(result).isTrue();
            ArgumentCaptor<QuizOption> captor = ArgumentCaptor.forClass(QuizOption.class);
            verify(quizOptionService).save(captor.capture());
            assertThat(captor.getValue().getQuizId()).isEqualTo(1L);
            assertThat(captor.getValue().getOptionText()).isEqualTo("新选项");
            assertThat(captor.getValue().getIsCorrect()).isFalse();
            assertThat(captor.getValue().getOrderIndex()).isEqualTo("B");
        }

        @Test
        @DisplayName("从请求对象创建失败时返回 false")
        void createFromRequestReturnsFalseOnFailure() {
            QuizOptionRequest request = new QuizOptionRequest("新选项", false, "B");
            doReturn(false).when(quizOptionService).save(any(QuizOption.class));

            Boolean result = quizOptionService.create(1L, request);

            assertThat(result).isFalse();
        }
    }

    // ==================== getByQuizId ====================

    @Test
    @DisplayName("getByQuizId 根据测验 ID 返回选项列表")
    void getByQuizIdReturnsOptionList() {
        QuizOption opt1 = QuizOption.builder().id(1L).quizId(1L).optionText("A").build();
        QuizOption opt2 = QuizOption.builder().id(1L).quizId(1L).optionText("B").build();
        doReturn(List.of(opt1, opt2)).when(quizOptionService).list(any(Wrapper.class));

        List<QuizOption> result = quizOptionService.getByQuizId(1L);

        assertThat(result).hasSize(2);
        assertThat(result).extracting(QuizOption::getQuizId).containsOnly(1L);
    }

    @Test
    @DisplayName("getByQuizId 测验下无选项时返回空列表")
    void getByQuizIdReturnsEmptyListWhenNoOptions() {
        doReturn(Collections.emptyList()).when(quizOptionService).list(any(Wrapper.class));

        List<QuizOption> result = quizOptionService.getByQuizId(1L);

        assertThat(result).isEmpty();
    }

    // ==================== get ====================

    @Test
    @DisplayName("get 非学生用户直接返回选项（不隐藏正确答案）")
    void getReturnsOptionWithIsCorrectForNonStudent() {
        User schoolUser = User.builder()
                .id(1L)
                .username("admin")
                .userType(UserType.SCHOOL)
                .build();
        QuizOption option = QuizOption.builder()
                .id(1L)
                .quizId(1L)
                .optionText("正确答案")
                .isCorrect(true)
                .build();

        when(userService.getCurrentUser()).thenReturn(schoolUser);
        doReturn(option).when(quizOptionService).getById(1L);

        QuizOption result = quizOptionService.get(1L);

        assertThat(result).isNotNull();
        assertThat(result.getIsCorrect()).isTrue();
        verify(userQuizAnswerService, never()).getByUserIdAndQuizId(any(), any());
    }

    @Test
    @DisplayName("get 管理员用户直接返回选项（不隐藏正确答案）")
    void getReturnsOptionWithIsCorrectForManager() {
        User managerUser = User.builder()
                .id(1L)
                .username("manager")
                .userType(UserType.MANAGER)
                .build();
        QuizOption option = QuizOption.builder()
                .id(1L)
                .quizId(1L)
                .optionText("正确答案")
                .isCorrect(true)
                .build();

        when(userService.getCurrentUser()).thenReturn(managerUser);
        doReturn(option).when(quizOptionService).getById(1L);

        QuizOption result = quizOptionService.get(1L);

        assertThat(result).isNotNull();
        assertThat(result.getIsCorrect()).isTrue();
        verify(userQuizAnswerService, never()).getByUserIdAndQuizId(any(), any());
    }

    @Test
    @DisplayName("get 学生用户未答题时隐藏正确答案（isCorrect 设为 null）")
    void getHidesIsCorrectForStudentWhoHasNotAnswered() {
        User studentUser = User.builder()
                .id(1L)
                .username("student")
                .userType(UserType.STUDENT)
                .build();
        QuizOption option = QuizOption.builder()
                .id(1L)
                .quizId(1L)
                .optionText("正确答案")
                .isCorrect(true)
                .build();

        when(userService.getCurrentUser()).thenReturn(studentUser);
        doReturn(option).when(quizOptionService).getById(1L);
        when(userQuizAnswerService.getByUserIdAndQuizId(1L, 1L)).thenReturn(Collections.emptyList());

        QuizOption result = quizOptionService.get(1L);

        assertThat(result).isNotNull();
        assertThat(result.getIsCorrect()).isNull();
        assertThat(result.getOptionText()).isEqualTo("正确答案");
        verify(userQuizAnswerService).getByUserIdAndQuizId(1L, 1L);
    }

    @Test
    @DisplayName("get 学生用户已答题时保留正确答案")
    void getPreservesIsCorrectForStudentWhoHasAnswered() {
        User studentUser = User.builder()
                .id(1L)
                .username("student")
                .userType(UserType.STUDENT)
                .build();
        QuizOption option = QuizOption.builder()
                .id(1L)
                .quizId(1L)
                .optionText("正确答案")
                .isCorrect(true)
                .build();
        UserQuizAnswer answer =
                UserQuizAnswer.builder().id(1L).userId(1L).quizId(1L).build();

        when(userService.getCurrentUser()).thenReturn(studentUser);
        doReturn(option).when(quizOptionService).getById(1L);
        when(userQuizAnswerService.getByUserIdAndQuizId(1L, 1L)).thenReturn(List.of(answer));

        QuizOption result = quizOptionService.get(1L);

        assertThat(result).isNotNull();
        assertThat(result.getIsCorrect()).isTrue();
        verify(userQuizAnswerService).getByUserIdAndQuizId(1L, 1L);
    }

    @Test
    @DisplayName("get 选项不存在时抛出 BusinessException")
    void getThrowsBusinessExceptionWhenOptionNotFound() {
        User studentUser = User.builder()
                .id(1L)
                .username("student")
                .userType(UserType.STUDENT)
                .build();
        when(userService.getCurrentUser()).thenReturn(studentUser);
        doReturn(null).when(quizOptionService).getById(999L);

        assertThatThrownBy(() -> quizOptionService.get(999L))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", QuizErrorConstants.QUIZ_OPTION_NOT_FOUND);
        verify(userQuizAnswerService, never()).getByUserIdAndQuizId(any(), any());
    }

    // ==================== delete ====================

    @Test
    @DisplayName("delete 删除选项成功返回 true")
    void deleteReturnsTrueOnSuccess() {
        doReturn(true).when(quizOptionService).removeById(1L);

        Boolean result = quizOptionService.delete(1L);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("delete 删除失败时返回 false")
    void deleteReturnsFalseOnFailure() {
        doReturn(false).when(quizOptionService).removeById(1L);

        Boolean result = quizOptionService.delete(1L);

        assertThat(result).isFalse();
    }
}
