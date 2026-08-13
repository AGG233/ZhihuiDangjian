package com.rauio.smartdangjian.server.quiz.service.scorm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.quiz.constants.QuizErrorConstants;
import com.rauio.smartdangjian.server.quiz.mapper.ScormPackageMapper;
import com.rauio.smartdangjian.server.quiz.mapper.ScormRegistrationMapper;
import com.rauio.smartdangjian.server.quiz.pojo.entity.ScormPackage;
import com.rauio.smartdangjian.server.quiz.pojo.entity.ScormRegistration;
import com.rauio.smartdangjian.server.quiz.pojo.request.ScormSubmitRequest;
import com.rauio.smartdangjian.server.quiz.pojo.response.ScormSummaryResponse;
import com.rauio.smartdangjian.utils.SecurityUtils;

@ExtendWith(MockitoExtension.class)
class ScormRegistrationServiceTest {

    @Mock
    private ScormRegistrationMapper registrationMapper;

    @Mock
    private ScormPackageMapper scormPackageMapper;

    @Mock
    private ScormPackageService scormPackageService;

    @Spy
    @InjectMocks
    private ScormRegistrationService scormRegistrationService;

    // ==================== submit ====================

    @Test
    @DisplayName("submit 首次上报：插入新记录")
    void submitInsertsWhenNoExisting() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn("1");
            when(scormPackageService.getById(10L)).thenReturn(pkg(10L, "Course A"));
            doReturn(null).when(scormRegistrationService).getOne(any(Wrapper.class));
            doReturn(true).when(scormRegistrationService).save(any(ScormRegistration.class));

            ScormRegistration result = scormRegistrationService.submit(10L, request("sco-1", "completed", "90.00"));

            assertThat(result.getUserId()).isEqualTo(1L);
            assertThat(result.getPackageId()).isEqualTo(10L);
            assertThat(result.getScoIdentifier()).isEqualTo("sco-1");
            assertThat(result.getLessonStatus()).isEqualTo("completed");
            assertThat(result.getScoreRaw()).isEqualByComparingTo(new BigDecimal("90.00"));
            verify(scormRegistrationService).save(any(ScormRegistration.class));
            verify(scormRegistrationService, never()).updateById(any(ScormRegistration.class));
        }
    }

    @Test
    @DisplayName("submit 重复上报：按 user+package+sco 查重并更新已有记录")
    void submitUpdatesWhenExisting() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn("1");
            when(scormPackageService.getById(10L)).thenReturn(pkg(10L, "Course A"));
            doReturn(registration(99L, 1L, 10L, "sco-1"))
                    .when(scormRegistrationService)
                    .getOne(any(Wrapper.class));
            doReturn(true).when(scormRegistrationService).updateById(any(ScormRegistration.class));

            ScormRegistration result = scormRegistrationService.submit(10L, request("sco-1", "passed", "95.00"));

            assertThat(result.getId()).isEqualTo(99L);
            assertThat(result.getLessonStatus()).isEqualTo("passed");
            verify(scormRegistrationService).updateById(any(ScormRegistration.class));
            verify(scormRegistrationService, never()).save(any(ScormRegistration.class));
        }
    }

    @Test
    @DisplayName("submit 学习包不存在：抛 BusinessException 且错误码 SCORM_PACKAGE_NOT_FOUND")
    void submitThrowsWhenPackageNotFound() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn("1");
            when(scormPackageService.getById(999L)).thenReturn(null);

            assertThatThrownBy(() -> scormRegistrationService.submit(999L, request("sco-1", "completed", null)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getCode())
                            .isEqualTo(QuizErrorConstants.SCORM_PACKAGE_NOT_FOUND));
            verify(scormRegistrationService, never()).save(any(ScormRegistration.class));
            verify(scormRegistrationService, never()).updateById(any(ScormRegistration.class));
        }
    }

    @Test
    @DisplayName("submit 未登录：抛 BusinessException 且错误码 RESOURCE_NOT_AUTHORIZED")
    void submitThrowsWhenNotLoggedIn() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(null);

            assertThatThrownBy(() -> scormRegistrationService.submit(10L, request("sco-1", "completed", null)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getCode())
                            .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED));
        }
    }

    @Test
    @DisplayName("submit 落库失败：抛 BusinessException 且错误码 SCORM_REGISTRATION_SAVE_FAILED")
    void submitThrowsWhenSaveFails() {
        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserId).thenReturn("1");
            when(scormPackageService.getById(10L)).thenReturn(pkg(10L, "Course A"));
            doReturn(null).when(scormRegistrationService).getOne(any(Wrapper.class));
            doReturn(false).when(scormRegistrationService).save(any(ScormRegistration.class));

            assertThatThrownBy(() -> scormRegistrationService.submit(10L, request("sco-1", "completed", null)))
                    .isInstanceOf(BusinessException.class)
                    .satisfies(e -> assertThat(((BusinessException) e).getCode())
                            .isEqualTo(QuizErrorConstants.SCORM_REGISTRATION_SAVE_FAILED));
        }
    }

    // ==================== getSummary ====================

    @Test
    @DisplayName("getSummary 多包聚合：统计注册数/已完成数/平均分与标题")
    void getSummaryAggregatesByPackage() {
        doReturn(List.of(
                        registration(1L, 1L, 10L, "sco-a", "completed", "80"),
                        registration(2L, 1L, 10L, "sco-b", "completed", "90"),
                        registration(3L, 1L, 10L, "sco-c", "incomplete", "60"),
                        registration(4L, 1L, 20L, "sco-d", "completed", "75")))
                .when(scormRegistrationService)
                .list(any(Wrapper.class));
        when(scormPackageMapper.selectBatchIds(any())).thenReturn(List.of(pkg(10L, "Course A"), pkg(20L, "Course B")));

        List<ScormSummaryResponse> result = scormRegistrationService.getSummary(1L);

        assertThat(result).hasSize(2);
        ScormSummaryResponse courseA = result.get(0);
        assertThat(courseA.getPackageId()).isEqualTo(10L);
        assertThat(courseA.getTitle()).isEqualTo("Course A");
        assertThat(courseA.getRegistrationCount()).isEqualTo(3);
        assertThat(courseA.getCompletedCount()).isEqualTo(2);
        assertThat(courseA.getAvgScore()).isEqualByComparingTo(new BigDecimal("76.67"));
        ScormSummaryResponse courseB = result.get(1);
        assertThat(courseB.getPackageId()).isEqualTo(20L);
        assertThat(courseB.getRegistrationCount()).isEqualTo(1);
        assertThat(courseB.getCompletedCount()).isEqualTo(1);
        assertThat(courseB.getAvgScore()).isEqualByComparingTo(new BigDecimal("75.00"));
    }

    @Test
    @DisplayName("getSummary 无记录：返回空列表")
    void getSummaryReturnsEmptyWhenNoRegistrations() {
        doReturn(List.of()).when(scormRegistrationService).list(any(Wrapper.class));

        List<ScormSummaryResponse> result = scormRegistrationService.getSummary(1L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("getSummary 无分数记录时平均分为 0")
    void getSummaryAvgScoreZeroWhenAllScoresNull() {
        doReturn(List.of(
                        registration(1L, 1L, 10L, "sco-a", "completed", null),
                        registration(2L, 1L, 10L, "sco-b", "incomplete", null)))
                .when(scormRegistrationService)
                .list(any(Wrapper.class));
        when(scormPackageMapper.selectBatchIds(any())).thenReturn(List.of(pkg(10L, "Course A")));

        List<ScormSummaryResponse> result = scormRegistrationService.getSummary(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAvgScore()).isEqualByComparingTo(BigDecimal.ZERO.setScale(2));
    }

    // ==================== helpers ====================

    private static ScormPackage pkg(Long id, String title) {
        return ScormPackage.builder().id(id).title(title).build();
    }

    private static ScormRegistration registration(Long id, Long userId, Long packageId, String scoIdentifier) {
        return ScormRegistration.builder()
                .id(id)
                .userId(userId)
                .packageId(packageId)
                .scoIdentifier(scoIdentifier)
                .build();
    }

    private static ScormRegistration registration(
            Long id, Long userId, Long packageId, String scoIdentifier, String lessonStatus, String scoreRaw) {
        return ScormRegistration.builder()
                .id(id)
                .userId(userId)
                .packageId(packageId)
                .scoIdentifier(scoIdentifier)
                .lessonStatus(lessonStatus)
                .scoreRaw(scoreRaw == null ? null : new BigDecimal(scoreRaw))
                .build();
    }

    private static ScormSubmitRequest request(String scoIdentifier, String lessonStatus, String scoreRaw) {
        return ScormSubmitRequest.builder()
                .scoIdentifier(scoIdentifier)
                .lessonStatus(lessonStatus)
                .scoreRaw(scoreRaw == null ? null : new BigDecimal(scoreRaw))
                .sessionTimeSeconds(300)
                .totalTimeSeconds(600)
                .build();
    }
}
