package com.rauio.smartdangjian.crosslayer.quiz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.quiz.mapper.UserQuizAnswerMapper;
import com.rauio.smartdangjian.server.quiz.pojo.dto.ChapterAccuracyRow;
import com.rauio.smartdangjian.server.quiz.pojo.response.ChapterAccuracyResponse;
import com.rauio.smartdangjian.server.quiz.service.UserQuizAnswerService;

/**
 * 按章节答题准确率聚合跨层回归测试。
 *
 * <p>装配真实 {@link UserQuizAnswerService}（含 {@code @Transactional} 代理与 baseMapper
 * 注入路径），Mapper 以 {@link MockitoBean} 提供（Spring 自动用例间重置，沿用 T1
 * UserQuizAnswerScoringCrossLayerTest 约定）。聚合 SQL 本身的执行正确性由
 * {@link ChapterAccuracyAggregationSqlTest} 针对真实 H2 验证。
 */
@SpringBootTest(classes = UserQuizAnswerAccuracyCrossLayerTest.TestConfig.class)
class UserQuizAnswerAccuracyCrossLayerTest extends CrossLayerTestBase {

    @MockitoBean
    private UserQuizAnswerMapper userQuizAnswerMapper;

    @Autowired
    private UserQuizAnswerService userQuizAnswerService;

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        UserQuizAnswerService userQuizAnswerService(UserQuizAnswerMapper userQuizAnswerMapper) {
            // getAccuracyByChapter 仅走 Mapper 聚合，不依赖判分服务，构造器依赖传 null
            return injectBaseMapper(new UserQuizAnswerService(null, null), userQuizAnswerMapper);
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

    @Test
    @DisplayName("多章节聚合：2 章节记录映射为 2 条准确率（题目数/答对数/正确率）")
    void groupsByChapterAcrossMultipleChapters() {
        when(userQuizAnswerMapper.selectChapterAccuracyByUserId(1L))
                .thenReturn(List.of(row(10L, 4, 3), row(20L, 2, 2)));

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
    }

    @Test
    @DisplayName("无答题记录：返回空列表")
    void returnsEmptyWhenNoAnswers() {
        when(userQuizAnswerMapper.selectChapterAccuracyByUserId(1L)).thenReturn(Collections.emptyList());

        List<ChapterAccuracyResponse> result = userQuizAnswerService.getAccuracyByChapter(1L);

        assertThat(result).isEmpty();
    }

    private ChapterAccuracyRow row(Long chapterId, Integer questionCount, Integer correctCount) {
        ChapterAccuracyRow row = new ChapterAccuracyRow();
        row.setChapterId(chapterId);
        row.setQuestionCount(questionCount);
        row.setCorrectCount(correctCount);
        return row;
    }
}
