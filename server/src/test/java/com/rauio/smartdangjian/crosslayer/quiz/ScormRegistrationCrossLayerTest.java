package com.rauio.smartdangjian.crosslayer.quiz;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
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

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.quiz.constants.QuizErrorConstants;
import com.rauio.smartdangjian.server.quiz.mapper.ScormPackageMapper;
import com.rauio.smartdangjian.server.quiz.mapper.ScormRegistrationMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.ScormPackage;
import com.rauio.smartdangjian.server.quiz.pojo.entity.ScormRegistration;
import com.rauio.smartdangjian.server.quiz.pojo.request.ScormSubmitRequest;
import com.rauio.smartdangjian.server.quiz.pojo.response.ScormSummaryResponse;
import com.rauio.smartdangjian.server.quiz.service.scorm.ScormPackageService;
import com.rauio.smartdangjian.server.quiz.service.scorm.ScormRegistrationService;

/**
 * SCORM 上报→汇总跨层回归测试。
 *
 * <p>装配真实 {@link ScormRegistrationService} 与 {@link ScormPackageService}，
 * Mapper 以 {@link MockitoBean} 提供，并用内存 store 模拟 Mapper 的持久化行为
 * （insert/updateById/selectList），selectOne 按用例显式驱动 upsert 分支，
 * 验证 submit 幂等 upsert 后 getSummary 能聚合出最新分数与完成数（数据闭环）。
 */
@SpringBootTest(classes = ScormRegistrationCrossLayerTest.TestConfig.class)
class ScormRegistrationCrossLayerTest extends CrossLayerTestBase {

    @MockitoBean
    private ScormRegistrationMapper scormRegistrationMapper;

    @MockitoBean
    private ScormPackageMapper scormPackageMapper;

    @Autowired
    private ScormRegistrationService scormRegistrationService;

    /** 模拟 scorm_registration 表的内存数据 */
    private final List<ScormRegistration> store = new ArrayList<>();

    @BeforeAll
    static void initMybatisPlus() {
        MybatisConfiguration config = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(config, "");
        TableInfoHelper.initTableInfo(assistant, ScormRegistration.class);
        TableInfoHelper.initTableInfo(assistant, ScormPackage.class);
    }

    @BeforeEach
    void stubMapperStore() {
        store.clear();
        when(scormRegistrationMapper.insert(any(ScormRegistration.class))).thenAnswer(invocation -> {
            store.add(invocation.getArgument(0));
            return 1;
        });
        when(scormRegistrationMapper.updateById(any(ScormRegistration.class))).thenAnswer(invocation -> {
            ScormRegistration updated = invocation.getArgument(0);
            store.removeIf(registration -> registration.getScoIdentifier().equals(updated.getScoIdentifier()));
            store.add(updated);
            return 1;
        });
        when(scormRegistrationMapper.selectList(any())).thenAnswer(invocation -> new ArrayList<>(store));
        when(scormPackageMapper.selectBatchIds(any())).thenReturn(List.of(packageEntity()));
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        ScormPackageService scormPackageService(ScormPackageMapper scormPackageMapper) {
            return injectBaseMapper(new ScormPackageService(), scormPackageMapper);
        }

        @Bean
        ScormRegistrationService scormRegistrationService(
                ScormPackageService scormPackageService,
                ScormPackageMapper scormPackageMapper,
                ScormRegistrationMapper scormRegistrationMapper) {
            return injectBaseMapper(
                    new ScormRegistrationService(scormPackageService, scormPackageMapper), scormRegistrationMapper);
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
    @DisplayName("同 SCO 提交两次不同分数：summary 反映最新分数且注册数不重复")
    void resubmitSameScoReflectsLatestScore() {
        setStudentContext(1L, "uni1");
        when(scormPackageMapper.selectById(1L)).thenReturn(packageEntity());

        // 首次提交：无存量记录 → insert
        when(scormRegistrationMapper.selectOne(any(), anyBoolean())).thenReturn(null);
        scormRegistrationService.submit(1L, request("sco-1", "in_progress", "60.00"));
        assertThat(store).hasSize(1);

        // 二次提交同 SCO：查到存量记录 → update，最新分数覆盖旧值
        doAnswer(invocation -> store.get(0)).when(scormRegistrationMapper).selectOne(any(), anyBoolean());
        scormRegistrationService.submit(1L, request("sco-1", "completed", "90.00"));
        assertThat(store).hasSize(1);
        assertThat(store.get(0).getScoreRaw()).isEqualByComparingTo("90.00");
        assertThat(store.get(0).getLessonStatus()).isEqualTo("completed");

        List<ScormSummaryResponse> summaries = scormRegistrationService.getSummary(1L);
        assertThat(summaries).hasSize(1);
        ScormSummaryResponse summary = summaries.get(0);
        assertThat(summary.getRegistrationCount()).isEqualTo(1);
        assertThat(summary.getCompletedCount()).isEqualTo(1);
        assertThat(summary.getAvgScore()).isEqualByComparingTo("90.00");
    }

    @Test
    @DisplayName("两个 SCO 各自上报：summary 聚合注册数、完成数与平均分")
    void submitTwoScosAggregatesSummary() {
        setStudentContext(1L, "uni1");
        when(scormPackageMapper.selectById(1L)).thenReturn(packageEntity());
        when(scormRegistrationMapper.selectOne(any(), anyBoolean())).thenReturn(null);

        scormRegistrationService.submit(1L, request("sco-1", "completed", "80.00"));
        scormRegistrationService.submit(1L, request("sco-2", "completed", "70.00"));

        List<ScormSummaryResponse> summaries = scormRegistrationService.getSummary(1L);
        assertThat(summaries).hasSize(1);
        ScormSummaryResponse summary = summaries.get(0);
        assertThat(summary.getRegistrationCount()).isEqualTo(2);
        assertThat(summary.getCompletedCount()).isEqualTo(2);
        assertThat(summary.getAvgScore()).isEqualByComparingTo("75.00");
    }

    @Test
    @DisplayName("上报记录归属当前登录用户（SecurityUtils 取 userId）")
    void registrationBelongsToCurrentUser() {
        setStudentContext(42L, "uni1");
        when(scormPackageMapper.selectById(1L)).thenReturn(packageEntity());
        when(scormRegistrationMapper.selectOne(any(), anyBoolean())).thenReturn(null);

        scormRegistrationService.submit(1L, request("sco-1", "completed", "80.00"));

        assertThat(store).hasSize(1);
        assertThat(store.get(0).getUserId()).isEqualTo(42L);
    }

    // ==================== failure path ====================

    @Test
    @DisplayName("学习包不存在：submit 抛 BusinessException 且错误码 SCORM_PACKAGE_NOT_FOUND")
    void submitPackageNotFoundThrows() {
        setStudentContext(1L, "uni1");
        when(scormPackageMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> scormRegistrationService.submit(999L, request("sco-1", "completed", "80.00")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(QuizErrorConstants.SCORM_PACKAGE_NOT_FOUND));
        assertThat(store).isEmpty();
    }

    @Test
    @DisplayName("未登录：submit 抛 BusinessException 且错误码 RESOURCE_NOT_AUTHORIZED")
    void submitWithoutLoginThrowsNotAuthorized() {
        setAnonymousContext();

        assertThatThrownBy(() -> scormRegistrationService.submit(1L, request("sco-1", "completed", "80.00")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED));
        assertThat(store).isEmpty();
    }

    // ==================== helpers ====================

    private static ScormSubmitRequest request(String scoIdentifier, String lessonStatus, String scoreRaw) {
        return ScormSubmitRequest.builder()
                .scoIdentifier(scoIdentifier)
                .lessonStatus(lessonStatus)
                .scoreRaw(new BigDecimal(scoreRaw))
                .build();
    }

    private static ScormPackage packageEntity() {
        return ScormPackage.builder().id(1L).title("党史学习课程").version("2004").build();
    }
}
