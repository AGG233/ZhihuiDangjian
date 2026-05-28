package com.rauio.smartdangjian.server.learning.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.server.learning.pojo.request.UserLearningRecordRequest;
import com.rauio.smartdangjian.server.learning.pojo.response.UserLearningRecordResponse;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;

@ExtendWith(MockitoExtension.class)
class UserLearningRecordControllerTest {

    @Mock
    private UserLearningRecordService recordService;

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
                .thenReturn(List.of(UserLearningRecordResponse.builder().id(1L).build()));

        var result = controller.getByUserId(1L);

        assertThat(result.getData()).hasSize(1);
    }

    @Test
    @DisplayName("getByUserIdAndChapterId 委托 service 获取用户章节记录")
    void getByUserIdAndChapterId() {
        when(recordService.getByUserIdAndChapterId(1L, 2L))
                .thenReturn(List.of(UserLearningRecordResponse.builder().id(1L).build()));

        var result = controller.getByUserIdAndChapterId(1L, 2L);

        assertThat(result.getData()).hasSize(1);
    }

    @Test
    @DisplayName("create 委托 service 创建记录")
    void create() {
        UserLearningRecordRequest dto =
                UserLearningRecordRequest.builder().userId(1L).chapterId(1L).build();
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
}
