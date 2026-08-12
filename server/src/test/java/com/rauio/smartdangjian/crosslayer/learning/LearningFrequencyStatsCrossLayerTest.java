package com.rauio.smartdangjian.crosslayer.learning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
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
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.graph.service.KnowledgeGraphService;
import com.rauio.smartdangjian.server.learning.constants.LearningErrorConstants;
import com.rauio.smartdangjian.server.learning.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.server.learning.pojo.convertor.UserLearningRecordConvertor;
import com.rauio.smartdangjian.server.learning.pojo.response.DayFrequencyStat;
import com.rauio.smartdangjian.server.learning.pojo.response.FrequencyStatsResponse;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;

/**
 * 碎片化学习频率统计跨层回归测试。
 *
 * <p>装配真实 UserLearningRecordService，Mapper 以 {@link MockitoBean} 提供
 * （Spring 在用例之间自动重置，沿用既有 CrossLayerTestBase 约定：H2 URL +
 * Flyway 禁用 + 真实 Service）。通过桩 selectFrequencyStats 返回多天聚合行，
 * 断言 Service 层汇总逻辑（总次数 / 总时长 / 日均频次）正确。
 */
@SpringBootTest(classes = LearningFrequencyStatsCrossLayerTest.TestConfig.class)
class LearningFrequencyStatsCrossLayerTest extends CrossLayerTestBase {

    private static final Long USER_ID = 1L;

    @MockitoBean
    private UserLearningRecordMapper learningRecordMapper;

    @MockitoBean
    private KnowledgeGraphService knowledgeGraphService;

    @Autowired
    private UserLearningRecordService recordService;

    @BeforeEach
    void stubFrequencyStats() {
        when(learningRecordMapper.selectFrequencyStats(any(Long.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        UserLearningRecordConvertor userLearningRecordConvertor() {
            return mock(UserLearningRecordConvertor.class);
        }

        @Bean
        UserLearningRecordService userLearningRecordService(
                UserLearningRecordConvertor convertor,
                KnowledgeGraphService knowledgeGraphService,
                UserLearningRecordMapper userLearningRecordMapper) {
            return injectBaseMapper(
                    new UserLearningRecordService(convertor, knowledgeGraphService), userLearningRecordMapper);
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
                throw new RuntimeException("Failed to set baseMapper on " + service.getClass().getSimpleName(), e);
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
    @DisplayName("3天各2条记录：返回3天明细、总次数6、日均频次按窗口天数计算")
    void multiDayRecordsAggregateCorrectly() {
        when(learningRecordMapper.selectFrequencyStats(any(Long.class), any(LocalDateTime.class)))
                .thenReturn(List.of(
                        stat(LocalDate.of(2026, 8, 10), 2L, 1200L),
                        stat(LocalDate.of(2026, 8, 11), 2L, 1800L),
                        stat(LocalDate.of(2026, 8, 12), 2L, 2400L)));

        FrequencyStatsResponse result = recordService.getFrequencyStats(USER_ID, 3);

        assertThat(result.getDays()).hasSize(3);
        assertThat(result.getTotalCount()).isEqualTo(6);
        assertThat(result.getTotalDuration()).isEqualTo(5400);
        assertThat(result.getAvgPerDay()).isCloseTo(2.0, within(0.0001));
    }

    @Test
    @DisplayName("无记录：返回空明细与零值汇总，不报错")
    void noRecordsReturnsEmptyAndZeroes() {
        FrequencyStatsResponse result = recordService.getFrequencyStats(USER_ID, 30);

        assertThat(result.getDays()).isEmpty();
        assertThat(result.getTotalCount()).isZero();
        assertThat(result.getTotalDuration()).isZero();
        assertThat(result.getAvgPerDay()).isZero();
    }

    @Test
    @DisplayName("days 超过365：抛 BusinessException 4006")
    void daysOverLimitThrowsBusinessException() {
        assertThatThrownBy(() -> recordService.getFrequencyStats(USER_ID, 366))
                .isInstanceOf(BusinessException.class)
                .satisfies(e ->
                        assertThat(((BusinessException) e).getCode())
                                .isEqualTo(LearningErrorConstants.STATS_DAYS_OUT_OF_RANGE));
    }

    private DayFrequencyStat stat(LocalDate date, long count, long totalDuration) {
        return DayFrequencyStat.builder()
                .date(date)
                .recordCount(count)
                .totalDuration(totalDuration)
                .build();
    }
}
