package com.rauio.smartdangjian.server.quiz.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.quiz.constants.QuizErrorConstants;
import com.rauio.smartdangjian.server.quiz.mapper.UserQuizAnswerMapper;
import com.rauio.smartdangjian.server.quiz.pojo.dto.ChapterAccuracyRow;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;
import com.rauio.smartdangjian.server.quiz.pojo.response.ChapterAccuracyResponse;

@ExtendWith(MockitoExtension.class)
class UserQuizAnswerServiceTest {

    @Mock
    private UserQuizAnswerMapper mapper;

    @Mock
    private QuizService quizService;

    @Mock
    private QuizOptionService quizOptionService;

    @Spy
    @InjectMocks
    private UserQuizAnswerService userQuizAnswerService;

    @BeforeEach
    void injectBaseMapper() throws Exception {
        // @Spy 实例无法经 @InjectMocks 注入父类 ServiceImpl.baseMapper，手动反射注入，
        // 供 getAccuracyByChapter 直接调用 mapper 聚合方法（既有用例不依赖 baseMapper，不受影响）
        Field baseMapperField = findBaseMapperField(UserQuizAnswerService.class);
        baseMapperField.setAccessible(true);
        baseMapperField.set(userQuizAnswerService, mapper);
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

    // ==================== create ====================

    @Test
    @DisplayName("create 保存答题记录成功返回 true")
    void createReturnsTrueOnSuccess() {
        UserQuizAnswer answer =
                UserQuizAnswer.builder().userId(1L).quizId(1L).optionId(1L).build();
        Quiz quiz = quiz("single_choice", 5);
        QuizOption option = option(1L, 1L, true);
        when(quizService.get(1L)).thenReturn(quiz);
        when(quizOptionService.getById(1L)).thenReturn(option);
        doReturn(true).when(userQuizAnswerService).save(any(UserQuizAnswer.class));

        Boolean result = userQuizAnswerService.create(answer);

        assertThat(result).isTrue();
        assertThat(answer.getIsCorrect()).isEqualTo(1);
        assertThat(answer.getScoreObtained()).isEqualTo(5);
    }

    @Test
    @DisplayName("create 保存失败时返回 false")
    void createReturnsFalseOnFailure() {
        UserQuizAnswer answer =
                UserQuizAnswer.builder().userId(1L).quizId(1L).optionId(1L).build();
        Quiz quiz = quiz("single_choice", 5);
        QuizOption option = option(1L, 1L, true);
        when(quizService.get(1L)).thenReturn(quiz);
        when(quizOptionService.getById(1L)).thenReturn(option);
        doReturn(false).when(userQuizAnswerService).save(any(UserQuizAnswer.class));

        Boolean result = userQuizAnswerService.create(answer);

        assertThat(result).isFalse();
    }

    // ==================== create 自动判分 ====================

    @Test
    @DisplayName("create 单选题答对：isCorrect=1、scoreObtained=题目分值")
    void createSingleChoiceCorrect() {
        UserQuizAnswer answer = answer(1L, 1L, 1L);
        when(quizService.get(1L)).thenReturn(quiz("single_choice", 5));
        when(quizOptionService.getById(1L)).thenReturn(option(1L, 1L, true));
        doReturn(true).when(userQuizAnswerService).save(any(UserQuizAnswer.class));

        Boolean result = userQuizAnswerService.create(answer);

        assertThat(result).isTrue();
        assertThat(answer.getIsCorrect()).isEqualTo(1);
        assertThat(answer.getScoreObtained()).isEqualTo(5);
    }

    @Test
    @DisplayName("create 单选题答错：isCorrect=0、scoreObtained=0")
    void createSingleChoiceWrong() {
        UserQuizAnswer answer = answer(1L, 1L, 2L);
        when(quizService.get(1L)).thenReturn(quiz("single_choice", 5));
        when(quizOptionService.getById(2L)).thenReturn(option(2L, 1L, false));
        doReturn(true).when(userQuizAnswerService).save(any(UserQuizAnswer.class));

        userQuizAnswerService.create(answer);

        assertThat(answer.getIsCorrect()).isEqualTo(0);
        assertThat(answer.getScoreObtained()).isZero();
    }

    @Test
    @DisplayName("create 判断题答对：isCorrect=1、scoreObtained=题目分值")
    void createTrueFalseCorrect() {
        UserQuizAnswer answer = answer(1L, 1L, 1L);
        when(quizService.get(1L)).thenReturn(quiz("true_false", 2));
        when(quizOptionService.getById(1L)).thenReturn(option(1L, 1L, true));
        doReturn(true).when(userQuizAnswerService).save(any(UserQuizAnswer.class));

        userQuizAnswerService.create(answer);

        assertThat(answer.getIsCorrect()).isEqualTo(1);
        assertThat(answer.getScoreObtained()).isEqualTo(2);
    }

    @Test
    @DisplayName("create 判断题答错：isCorrect=0、scoreObtained=0")
    void createTrueFalseWrong() {
        UserQuizAnswer answer = answer(1L, 1L, 2L);
        when(quizService.get(1L)).thenReturn(quiz("true_false", 2));
        when(quizOptionService.getById(2L)).thenReturn(option(2L, 1L, false));
        doReturn(true).when(userQuizAnswerService).save(any(UserQuizAnswer.class));

        userQuizAnswerService.create(answer);

        assertThat(answer.getIsCorrect()).isEqualTo(0);
        assertThat(answer.getScoreObtained()).isZero();
    }

    @Test
    @DisplayName("create 多选题部分命中：isCorrect=2、scoreObtained 按命中比例四舍五入")
    void createMultipleChoicePartial() {
        // 正确选项为 A、B，本次只提交 A → 5 * 1 / 2 = 2.5 → 四舍五入 3
        UserQuizAnswer answer = answer(1L, 1L, 1L);
        when(quizService.get(1L)).thenReturn(quiz("multiple_choice", 5));
        when(quizOptionService.getById(1L)).thenReturn(option(1L, 1L, true));
        when(quizOptionService.getByQuizId(1L)).thenReturn(List.of(option(1L, 1L, true), option(2L, 1L, true)));
        doReturn(Collections.emptyList()).when(userQuizAnswerService).getByUserIdAndQuizId(1L, 1L);
        doReturn(true).when(userQuizAnswerService).save(any(UserQuizAnswer.class));

        userQuizAnswerService.create(answer);

        assertThat(answer.getIsCorrect()).isEqualTo(2);
        assertThat(answer.getScoreObtained()).isEqualTo(3);
    }

    @Test
    @DisplayName("create 多选题全部命中且无多选：isCorrect=1 满分")
    void createMultipleChoiceAllCorrect() {
        // 已提交正确选项 A，本次提交正确选项 B → 全部命中
        UserQuizAnswer answer = answer(1L, 1L, 2L);
        when(quizService.get(1L)).thenReturn(quiz("multiple_choice", 5));
        when(quizOptionService.getById(2L)).thenReturn(option(2L, 1L, true));
        when(quizOptionService.getByQuizId(1L)).thenReturn(List.of(option(1L, 1L, true), option(2L, 1L, true)));
        doReturn(List.of(answer(1L, 1L, 1L))).when(userQuizAnswerService).getByUserIdAndQuizId(1L, 1L);
        doReturn(true).when(userQuizAnswerService).save(any(UserQuizAnswer.class));

        userQuizAnswerService.create(answer);

        assertThat(answer.getIsCorrect()).isEqualTo(1);
        assertThat(answer.getScoreObtained()).isEqualTo(5);
    }

    @Test
    @DisplayName("create 多选题含错误选项：isCorrect=0、0 分")
    void createMultipleChoiceWithWrongOption() {
        // 本次提交错误选项 C
        UserQuizAnswer answer = answer(1L, 1L, 3L);
        when(quizService.get(1L)).thenReturn(quiz("multiple_choice", 5));
        when(quizOptionService.getById(3L)).thenReturn(option(3L, 1L, false));
        when(quizOptionService.getByQuizId(1L)).thenReturn(List.of(option(1L, 1L, true), option(2L, 1L, true)));
        doReturn(Collections.emptyList()).when(userQuizAnswerService).getByUserIdAndQuizId(1L, 1L);
        doReturn(true).when(userQuizAnswerService).save(any(UserQuizAnswer.class));

        userQuizAnswerService.create(answer);

        assertThat(answer.getIsCorrect()).isEqualTo(0);
        assertThat(answer.getScoreObtained()).isZero();
    }

    @Test
    @DisplayName("create 多选题此前已选错误选项：本次即使选对也判 0 分")
    void createMultipleChoiceWithPreviouslyWrongSelection() {
        // 此前已提交错误选项 C，本次提交正确选项 A → 整体判 0 分
        UserQuizAnswer answer = answer(1L, 1L, 1L);
        when(quizService.get(1L)).thenReturn(quiz("multiple_choice", 5));
        when(quizOptionService.getById(1L)).thenReturn(option(1L, 1L, true));
        when(quizOptionService.getByQuizId(1L)).thenReturn(List.of(option(1L, 1L, true), option(2L, 1L, true)));
        doReturn(List.of(answer(1L, 1L, 3L))).when(userQuizAnswerService).getByUserIdAndQuizId(1L, 1L);
        doReturn(true).when(userQuizAnswerService).save(any(UserQuizAnswer.class));

        userQuizAnswerService.create(answer);

        assertThat(answer.getIsCorrect()).isEqualTo(0);
        assertThat(answer.getScoreObtained()).isZero();
    }

    @Test
    @DisplayName("create 主观题不自动判分：isCorrect/scoreObtained 置 null")
    void createShortAnswerNotAutoScored() {
        UserQuizAnswer answer = answer(1L, 1L, 1L);
        when(quizService.get(1L)).thenReturn(quiz("short_answer", 5));
        doReturn(true).when(userQuizAnswerService).save(any(UserQuizAnswer.class));

        Boolean result = userQuizAnswerService.create(answer);

        assertThat(result).isTrue();
        assertThat(answer.getIsCorrect()).isNull();
        assertThat(answer.getScoreObtained()).isNull();
        // 主观题不查询选项，不参与判分
        verify(quizOptionService, never()).getById(anyLong());
    }

    @Test
    @DisplayName("create 题目分值为 null 时按 0 分判分")
    void createNullScoreTreatedAsZero() {
        UserQuizAnswer answer = answer(1L, 1L, 1L);
        when(quizService.get(1L)).thenReturn(quiz("single_choice", null));
        when(quizOptionService.getById(1L)).thenReturn(option(1L, 1L, true));
        doReturn(true).when(userQuizAnswerService).save(any(UserQuizAnswer.class));

        userQuizAnswerService.create(answer);

        assertThat(answer.getIsCorrect()).isEqualTo(1);
        assertThat(answer.getScoreObtained()).isZero();
    }

    // ==================== create 失败路径 ====================

    @Test
    @DisplayName("create 题目不存在：抛 BusinessException 且错误码 QUIZ_NOT_FOUND")
    void createQuizNotFoundThrows() {
        when(quizService.get(999L)).thenReturn(null);

        assertThatThrownBy(() -> userQuizAnswerService.create(answer(1L, 999L, 1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e ->
                        assertThat(((BusinessException) e).getCode()).isEqualTo(QuizErrorConstants.QUIZ_NOT_FOUND));
    }

    @Test
    @DisplayName("create 选项不存在：抛 BusinessException 且错误码 QUIZ_OPTION_NOT_FOUND")
    void createOptionNotFoundThrows() {
        when(quizService.get(1L)).thenReturn(quiz("single_choice", 5));
        when(quizOptionService.getById(999L)).thenReturn(null);

        assertThatThrownBy(() -> userQuizAnswerService.create(answer(1L, 1L, 999L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(QuizErrorConstants.QUIZ_OPTION_NOT_FOUND));
    }

    @Test
    @DisplayName("create 选项不属于该题：抛 BusinessException 且错误码 QUIZ_OPTION_NOT_FOUND")
    void createOptionBelongsToAnotherQuizThrows() {
        when(quizService.get(1L)).thenReturn(quiz("single_choice", 5));
        when(quizOptionService.getById(2L)).thenReturn(option(2L, 99L, true));

        assertThatThrownBy(() -> userQuizAnswerService.create(answer(1L, 1L, 2L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(QuizErrorConstants.QUIZ_OPTION_NOT_FOUND));
    }

    @Test
    @DisplayName("create 重复提交同一题：按现有语义再次保存返回 true")
    void createDuplicateSubmissionSavesAgain() {
        UserQuizAnswer answer = answer(1L, 1L, 1L);
        when(quizService.get(1L)).thenReturn(quiz("single_choice", 5));
        when(quizOptionService.getById(1L)).thenReturn(option(1L, 1L, true));
        doReturn(true).when(userQuizAnswerService).save(any(UserQuizAnswer.class));

        Boolean first = userQuizAnswerService.create(answer);
        Boolean second = userQuizAnswerService.create(answer);

        assertThat(first).isTrue();
        assertThat(second).isTrue();
        verify(userQuizAnswerService, times(2)).save(any(UserQuizAnswer.class));
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

    // ==================== getAccuracyByChapter ====================

    @Test
    @DisplayName("getAccuracyByChapter 多章节数据：按章节映射题目数、答对数与正确率")
    void getAccuracyByChapterGroupsByChapter() {
        when(mapper.selectChapterAccuracyByUserId(1L)).thenReturn(List.of(
                row(10L, 4, 3),
                row(20L, 2, 2)));

        List<ChapterAccuracyResponse> result = userQuizAnswerService.getAccuracyByChapter(1L);

        assertThat(result).hasSize(2);
        ChapterAccuracyResponse chapter1 = result.get(0);
        assertThat(chapter1.getChapterId()).isEqualTo(10L);
        assertThat(chapter1.getQuestionCount()).isEqualTo(4);
        assertThat(chapter1.getCorrectCount()).isEqualTo(3);
        assertThat(chapter1.getAccuracy()).isEqualTo(0.75);
        ChapterAccuracyResponse chapter2 = result.get(1);
        assertThat(chapter2.getChapterId()).isEqualTo(20L);
        assertThat(chapter2.getQuestionCount()).isEqualTo(2);
        assertThat(chapter2.getCorrectCount()).isEqualTo(2);
        assertThat(chapter2.getAccuracy()).isEqualTo(1.0);
        verify(mapper).selectChapterAccuracyByUserId(1L);
    }

    @Test
    @DisplayName("getAccuracyByChapter 无答题记录：返回空列表")
    void getAccuracyByChapterReturnsEmptyWhenNoAnswers() {
        when(mapper.selectChapterAccuracyByUserId(1L)).thenReturn(Collections.emptyList());

        List<ChapterAccuracyResponse> result = userQuizAnswerService.getAccuracyByChapter(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getAccuracyByChapter 题目数/答对数为 null 时按 0 处理")
    void getAccuracyByChapterNullCountsTreatedAsZero() {
        when(mapper.selectChapterAccuracyByUserId(1L)).thenReturn(List.of(row(10L, null, null)));

        List<ChapterAccuracyResponse> result = userQuizAnswerService.getAccuracyByChapter(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuestionCount()).isZero();
        assertThat(result.get(0).getCorrectCount()).isZero();
        assertThat(result.get(0).getAccuracy()).isZero();
    }

    @Test
    @DisplayName("getAccuracyByChapter 题目数为 0 时正确率为 0")
    void getAccuracyByChapterZeroQuestionCount() {
        when(mapper.selectChapterAccuracyByUserId(1L)).thenReturn(List.of(row(10L, 0, 0)));

        List<ChapterAccuracyResponse> result = userQuizAnswerService.getAccuracyByChapter(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuestionCount()).isZero();
        assertThat(result.get(0).getAccuracy()).isZero();
    }

    // ==================== helpers ====================

    private Quiz quiz(String questionType, Integer score) {
        return Quiz.builder().id(1L).questionType(questionType).score(score).build();
    }

    private QuizOption option(Long id, Long quizId, boolean isCorrect) {
        return QuizOption.builder().id(id).quizId(quizId).isCorrect(isCorrect).build();
    }

    private UserQuizAnswer answer(Long userId, Long quizId, Long optionId) {
        return UserQuizAnswer.builder()
                .userId(userId)
                .quizId(quizId)
                .optionId(optionId)
                .build();
    }

    private ChapterAccuracyRow row(Long chapterId, Integer questionCount, Integer correctCount) {
        ChapterAccuracyRow row = new ChapterAccuracyRow();
        row.setChapterId(chapterId);
        row.setQuestionCount(questionCount);
        row.setCorrectCount(correctCount);
        return row;
    }
}
