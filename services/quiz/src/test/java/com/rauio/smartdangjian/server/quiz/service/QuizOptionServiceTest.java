package com.rauio.smartdangjian.server.quiz.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.rauio.smartdangjian.server.quiz.mapper.QuizOptionMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;
import com.rauio.smartdangjian.utils.spec.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    // ==================== update ====================

    @Test
    @DisplayName("update 设置选项 ID 后调用 updateById 成功返回 true")
    void updateSetsIdAndReturnsTrueOnSuccess() {
        QuizOption option = QuizOption.builder()
                .optionText("选项A")
                .isCorrect(true)
                .build();
        doReturn(true).when(quizOptionService).updateById(option);

        Boolean result = quizOptionService.update("opt-1", option);

        assertThat(result).isTrue();
        assertThat(option.getId()).isEqualTo("opt-1");
    }

    @Test
    @DisplayName("update 更新失败时返回 false")
    void updateReturnsFalseOnFailure() {
        QuizOption option = QuizOption.builder().optionText("选项A").build();
        doReturn(false).when(quizOptionService).updateById(option);

        Boolean result = quizOptionService.update("opt-1", option);

        assertThat(result).isFalse();
    }

    // ==================== create ====================

    @Test
    @DisplayName("create 设置 quizId 后调用 save 成功返回 true")
    void createSetsQuizIdAndReturnsTrueOnSuccess() {
        QuizOption option = QuizOption.builder()
                .optionText("新选项")
                .isCorrect(false)
                .build();
        doReturn(true).when(quizOptionService).save(option);

        Boolean result = quizOptionService.create("quiz-1", option);

        assertThat(result).isTrue();
        assertThat(option.getQuizId()).isEqualTo("quiz-1");
    }

    @Test
    @DisplayName("create 保存失败时返回 false")
    void createReturnsFalseOnFailure() {
        QuizOption option = QuizOption.builder().optionText("新选项").build();
        doReturn(false).when(quizOptionService).save(option);

        Boolean result = quizOptionService.create("quiz-1", option);

        assertThat(result).isFalse();
    }

    // ==================== getByQuizId ====================

    @Test
    @DisplayName("getByQuizId 根据测验 ID 返回选项列表")
    void getByQuizIdReturnsOptionList() {
        QuizOption opt1 = QuizOption.builder().id("opt-1").quizId("quiz-1").optionText("A").build();
        QuizOption opt2 = QuizOption.builder().id("opt-2").quizId("quiz-1").optionText("B").build();
        doReturn(List.of(opt1, opt2)).when(quizOptionService).list(any(Wrapper.class));

        List<QuizOption> result = quizOptionService.getByQuizId("quiz-1");

        assertThat(result).hasSize(2);
        assertThat(result).extracting(QuizOption::getQuizId).containsOnly("quiz-1");
    }

    @Test
    @DisplayName("getByQuizId 测验下无选项时返回空列表")
    void getByQuizIdReturnsEmptyListWhenNoOptions() {
        doReturn(Collections.emptyList()).when(quizOptionService).list(any(Wrapper.class));

        List<QuizOption> result = quizOptionService.getByQuizId("empty-quiz");

        assertThat(result).isEmpty();
    }

    // ==================== get ====================

    @Test
    @DisplayName("get 非学生用户直接返回选项（不隐藏正确答案）")
    void getReturnsOptionWithIsCorrectForNonStudent() {
        User schoolUser = User.builder()
                .id("user-1")
                .username("admin")
                .userType(UserType.SCHOOL)
                .build();
        QuizOption option = QuizOption.builder()
                .id("opt-1")
                .quizId("quiz-1")
                .optionText("正确答案")
                .isCorrect(true)
                .build();

        when(userService.getCurrentUser()).thenReturn(schoolUser);
        doReturn(option).when(quizOptionService).getById("opt-1");

        QuizOption result = quizOptionService.get("opt-1");

        assertThat(result).isNotNull();
        assertThat(result.getIsCorrect()).isTrue();
        verify(userQuizAnswerService, never()).getByUserIdAndQuizId(any(), any());
    }

    @Test
    @DisplayName("get 管理员用户直接返回选项（不隐藏正确答案）")
    void getReturnsOptionWithIsCorrectForManager() {
        User managerUser = User.builder()
                .id("user-2")
                .username("manager")
                .userType(UserType.MANAGER)
                .build();
        QuizOption option = QuizOption.builder()
                .id("opt-1")
                .quizId("quiz-1")
                .optionText("正确答案")
                .isCorrect(true)
                .build();

        when(userService.getCurrentUser()).thenReturn(managerUser);
        doReturn(option).when(quizOptionService).getById("opt-1");

        QuizOption result = quizOptionService.get("opt-1");

        assertThat(result).isNotNull();
        assertThat(result.getIsCorrect()).isTrue();
        verify(userQuizAnswerService, never()).getByUserIdAndQuizId(any(), any());
    }

    @Test
    @DisplayName("get 学生用户未答题时隐藏正确答案（isCorrect 设为 null）")
    void getHidesIsCorrectForStudentWhoHasNotAnswered() {
        User studentUser = User.builder()
                .id("student-1")
                .username("student")
                .userType(UserType.STUDENT)
                .build();
        QuizOption option = QuizOption.builder()
                .id("opt-1")
                .quizId("quiz-1")
                .optionText("正确答案")
                .isCorrect(true)
                .build();

        when(userService.getCurrentUser()).thenReturn(studentUser);
        doReturn(option).when(quizOptionService).getById("opt-1");
        when(userQuizAnswerService.getByUserIdAndQuizId("student-1", "quiz-1"))
                .thenReturn(Collections.emptyList());

        QuizOption result = quizOptionService.get("opt-1");

        assertThat(result).isNotNull();
        assertThat(result.getIsCorrect()).isNull();
    }

    @Test
    @DisplayName("get 学生用户已答题时保留正确答案")
    void getPreservesIsCorrectForStudentWhoHasAnswered() {
        User studentUser = User.builder()
                .id("student-1")
                .username("student")
                .userType(UserType.STUDENT)
                .build();
        QuizOption option = QuizOption.builder()
                .id("opt-1")
                .quizId("quiz-1")
                .optionText("正确答案")
                .isCorrect(true)
                .build();
        UserQuizAnswer answer = UserQuizAnswer.builder()
                .id("answer-1")
                .userId("student-1")
                .quizId("quiz-1")
                .build();

        when(userService.getCurrentUser()).thenReturn(studentUser);
        doReturn(option).when(quizOptionService).getById("opt-1");
        when(userQuizAnswerService.getByUserIdAndQuizId("student-1", "quiz-1"))
                .thenReturn(List.of(answer));

        QuizOption result = quizOptionService.get("opt-1");

        assertThat(result).isNotNull();
        assertThat(result.getIsCorrect()).isTrue();
    }

    // ==================== delete ====================

    @Test
    @DisplayName("delete 删除选项成功返回 true")
    void deleteReturnsTrueOnSuccess() {
        doReturn(true).when(quizOptionService).removeById("opt-1");

        Boolean result = quizOptionService.delete("opt-1");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("delete 删除失败时返回 false")
    void deleteReturnsFalseOnFailure() {
        doReturn(false).when(quizOptionService).removeById("opt-1");

        Boolean result = quizOptionService.delete("opt-1");

        assertThat(result).isFalse();
    }
}
