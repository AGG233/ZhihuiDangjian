package com.rauio.smartdangjian.server.learning.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.learning.pojo.request.UserLearningRecordRequest;
import com.rauio.smartdangjian.server.learning.pojo.response.DayFrequencyStat;
import com.rauio.smartdangjian.server.learning.pojo.response.FrequencyStatsResponse;
import com.rauio.smartdangjian.server.learning.pojo.response.UserLearningRecordResponse;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;
import com.rauio.smartdangjian.server.user.service.UserService;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class UserLearningRecordControllerTest {

    @Mock
    private UserLearningRecordService recordService;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserLearningRecordController controller;

    @Test
    @DisplayName("get 委托 service 获取学习记录")
    void get() {
        UserLearningRecordResponse vo =
                UserLearningRecordResponse.builder().id(1L).build();
        when(recordService.get(1L)).thenReturn(vo);

        var result = controller.get(1L);

        assertThat(result.getData().getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getByUserId 委托 service 获取用户所有记录")
    void getByUserId() {
        when(recordService.getByUserId(1L))
                .thenReturn(
                        List.of(UserLearningRecordResponse.builder().id(1L).build()));

        var result = controller.getByUserId(1L);

        assertThat(result.getData()).hasSize(1);
    }

    @Test
    @DisplayName("getByUserIdAndChapterId 委托 service 获取用户章节记录")
    void getByUserIdAndChapterId() {
        when(recordService.getByUserIdAndChapterId(1L, 2L))
                .thenReturn(
                        List.of(UserLearningRecordResponse.builder().id(1L).build()));

        var result = controller.getByUserIdAndChapterId(1L, 2L);

        assertThat(result.getData()).hasSize(1);
    }

    @Test
    @DisplayName("create 委托 service 创建记录")
    void create() {
        UserLearningRecordRequest dto = UserLearningRecordRequest.builder()
                .userId(1L)
                .chapterId(1L)
                .build();
        when(recordService.create(dto)).thenReturn(true);

        var result = controller.create(dto);

        assertThat(result.getData()).isTrue();
    }

    @Test
    @DisplayName("update 委托 service 更新记录")
    void update() {
        UserLearningRecordRequest dto =
                UserLearningRecordRequest.builder().id(1L).build();
        when(recordService.update(dto)).thenReturn(true);

        var result = controller.update(dto);

        assertThat(result.getData()).isTrue();
    }

    @Test
    @DisplayName("getFrequencyStats 使用当前登录用户ID委托 service 统计")
    void getFrequencyStatsDelegatesWithCurrentUser() {
        when(userService.getCurrentUserId()).thenReturn("1");
        FrequencyStatsResponse response = FrequencyStatsResponse.builder()
                .days(List.of(DayFrequencyStat.builder()
                        .date(LocalDate.of(2026, 8, 12))
                        .recordCount(2L)
                        .totalDuration(1200L)
                        .build()))
                .totalCount(2)
                .build();
        when(recordService.getFrequencyStats(1L, 7)).thenReturn(response);

        var result = controller.getFrequencyStats(7);

        assertThat(result.getData().getTotalCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("getFrequencyStats 未登录时抛出异常")
    void getFrequencyStatsThrowsWhenNotLoggedIn() {
        when(userService.getCurrentUserId()).thenReturn(null);

        assertThatThrownBy(() -> controller.getFrequencyStats(null))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未登录");
    }
}
