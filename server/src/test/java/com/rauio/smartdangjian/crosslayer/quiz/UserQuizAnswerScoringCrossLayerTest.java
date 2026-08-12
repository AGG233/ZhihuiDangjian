package com.rauio.smartdangjian.crosslayer.quiz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.quiz.constants.QuizErrorConstants;
import com.rauio.smartdangjian.server.quiz.mapper.QuizMapper;
import com.rauio.smartdangjian.server.quiz.mapper.UserQuizAnswerMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.Quiz;
import com.rauio.smartdangjian.server.quiz.pojo.entity.QuizOption;
import com.rauio.smartdangjian.server.quiz.pojo.entity.UserQuizAnswer;
import com.rauio.smartdangjian.server.quiz.service.QuizOptionService;
import com.rauio.smartdangjian.server.quiz.service.QuizService;
import com.rauio.smartdangjian.server.quiz.service.UserQuizAnswerService;

/**
 * 答题服务端自动判分跨层回归测试。
 *
 * <p>装配真实 UserQuizAnswerService 与 QuizService，Mapper 与 QuizOptionService
 * 以 {@link MockitoBean} 提供（Spring 在用例之间自动重置，沿用既有 CrossLayerTestBase
 * 约定：H2 URL + Flyway 禁用 + 真实 Service）。通过捕获真实 save() 最终写入 Mapper
 * 的实体，断言判分结果正确落库。
 */
@SpringBootTest(classes = UserQuizAnswerScoringCrossLayerTest.TestConfig.class)
class UserQuizAnswerScoringCrossLayerTest extends CrossLayerTestBase {

    @MockitoBean
    private UserQuizAnswerMapper userQuizAnswerMapper;

    @MockitoBean
    private QuizMapper quizMapper;

    @MockitoBean
    private QuizOptionService quizOptionService;

    @Autowired
    private UserQuizAnswerService userQuizAnswerService;

    @BeforeEach
    void stubInsertSuccess() {
        // 真实 ServiceImpl.save() 依赖 baseMapper.insert 返回行数，mock 默认 0 会让 save() 返回 false
        when(userQuizAnswerMapper.insert(any(UserQuizAnswer.class))).thenReturn(1);
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        QuizService quizService(QuizMapper quizMapper) {
            return injectBaseMapper(new QuizService(), quizMapper);
        }

        @Bean
        UserQuizAnswerService userQuizAnswerService(
                QuizService quizService,
                QuizOptionService quizOptionService,
                UserQuizAnswerMapper userQuizAnswerMapper) {
            return injectBaseMapper(new UserQuizAnswerService(quizService, quizOptionService), userQuizAnswerMapper);
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

        private static <T, M> T injectBaseMapper(T service, M mapper) {
            try {
                Field field = findBaseMapperField(service.getClass());
                field.setAccessible(true);
                field.set(service, mapper);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to set baseMapper on " + service.getClass().getSimpleName(), e);
            }
            return service;
        }

        @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
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

    // ==================== happy path ====================

    @Test
    @DisplayName("提交正确单选答案：落库 isCorrect=1、scoreObtained=题目分值")
    void singleChoiceCorrectPersistsScoredAnswer() {
        when(quizMapper.selectById(1L)).thenReturn(quiz("single_choice", 5));
        when(quizOptionService.getById(1L)).thenReturn(option(1L, 1L, true));

        Boolean result = userQuizAnswerService.create(answer(1L, 1L, 1L));

        assertThat(result).isTrue();
        UserQuizAnswer saved = captureInserted();
        assertThat(saved.getIsCorrect()).isEqualTo(1);
        assertThat(saved.getScoreObtained()).isEqualTo(5);
    }

    @Test
    @DisplayName("提交错误单选答案：落库 isCorrect=0、scoreObtained=0")
    void singleChoiceWrongPersistsZero() {
        when(quizMapper.selectById(1L)).thenReturn(quiz("single_choice", 5));
        when(quizOptionService.getById(2L)).thenReturn(option(2L, 1L, false));

        userQuizAnswerService.create(answer(1L, 1L, 2L));

        UserQuizAnswer saved = captureInserted();
        assertThat(saved.getIsCorrect()).isZero();
        assertThat(saved.getScoreObtained()).isZero();
    }

    @Test
    @DisplayName("提交多选题部分命中：落库 isCorrect=2、scoreObtained 按比例四舍五入")
    void multipleChoicePartialPersistsProportionalScore() {
        // 正确选项 A、B，只提交 A → 5 * 1 / 2 = 2.5 → 四舍五入 3
        when(quizMapper.selectById(1L)).thenReturn(quiz("multiple_choice", 5));
        when(quizOptionService.getById(1L)).thenReturn(option(1L, 1L, true));
        when(quizOptionService.getByQuizId(1L)).thenReturn(List.of(option(1L, 1L, true), option(2L, 1L, true)));
        when(userQuizAnswerMapper.selectList(any())).thenReturn(Collections.emptyList());

        userQuizAnswerService.create(answer(1L, 1L, 1L));

        UserQuizAnswer saved = captureInserted();
        assertThat(saved.getIsCorrect()).isEqualTo(2);
        assertThat(saved.getScoreObtained()).isEqualTo(3);
    }

    @Test
    @DisplayName("提交多选题全部命中且无多选：落库 isCorrect=1 满分")
    void multipleChoiceAllCorrectPersistsFullScore() {
        // 已提交正确选项 A，本次提交 B
        when(quizMapper.selectById(1L)).thenReturn(quiz("multiple_choice", 5));
        when(quizOptionService.getById(2L)).thenReturn(option(2L, 1L, true));
        when(quizOptionService.getByQuizId(1L)).thenReturn(List.of(option(1L, 1L, true), option(2L, 1L, true)));
        when(userQuizAnswerMapper.selectList(any())).thenReturn(List.of(answer(1L, 1L, 1L)));

        userQuizAnswerService.create(answer(1L, 1L, 2L));

        UserQuizAnswer saved = captureInserted();
        assertThat(saved.getIsCorrect()).isEqualTo(1);
        assertThat(saved.getScoreObtained()).isEqualTo(5);
    }

    @Test
    @DisplayName("提交主观题答案：落库 isCorrect/scoreObtained 置 null")
    void shortAnswerPersistsWithoutAutoScoring() {
        when(quizMapper.selectById(1L)).thenReturn(quiz("short_answer", 5));

        Boolean result = userQuizAnswerService.create(answer(1L, 1L, 1L));

        assertThat(result).isTrue();
        UserQuizAnswer saved = captureInserted();
        assertThat(saved.getIsCorrect()).isNull();
        assertThat(saved.getScoreObtained()).isNull();
    }

    @Test
    @DisplayName("重复提交同一题：按现有语义再次落库")
    void duplicateSubmissionInsertsTwice() {
        when(quizMapper.selectById(1L)).thenReturn(quiz("single_choice", 5));
        when(quizOptionService.getById(1L)).thenReturn(option(1L, 1L, true));

        Boolean first = userQuizAnswerService.create(answer(1L, 1L, 1L));
        Boolean second = userQuizAnswerService.create(answer(1L, 1L, 1L));

        assertThat(first).isTrue();
        assertThat(second).isTrue();
        verify(userQuizAnswerMapper, times(2)).insert(any(UserQuizAnswer.class));
    }

    // ==================== failure path ====================

    @Test
    @DisplayName("题目不存在：抛 BusinessException 且错误码 QUIZ_NOT_FOUND，不落库")
    void quizNotFoundThrowsAndDoesNotInsert() {
        when(quizMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> userQuizAnswerService.create(answer(1L, 999L, 1L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e ->
                        assertThat(((BusinessException) e).getCode()).isEqualTo(QuizErrorConstants.QUIZ_NOT_FOUND));
        verify(userQuizAnswerMapper, never()).insert(any(UserQuizAnswer.class));
    }

    @Test
    @DisplayName("选项不存在：抛 BusinessException 且错误码 QUIZ_OPTION_NOT_FOUND，不落库")
    void optionNotFoundThrowsAndDoesNotInsert() {
        when(quizMapper.selectById(1L)).thenReturn(quiz("single_choice", 5));
        when(quizOptionService.getById(999L)).thenReturn(null);

        assertThatThrownBy(() -> userQuizAnswerService.create(answer(1L, 1L, 999L)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(QuizErrorConstants.QUIZ_OPTION_NOT_FOUND));
        verify(userQuizAnswerMapper, never()).insert(any(UserQuizAnswer.class));
    }

    // ==================== helpers ====================

    private UserQuizAnswer captureInserted() {
        ArgumentCaptor<UserQuizAnswer> captor = ArgumentCaptor.forClass(UserQuizAnswer.class);
        verify(userQuizAnswerMapper).insert(captor.capture());
        return captor.getValue();
    }

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
}
