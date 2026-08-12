package com.rauio.smartdangjian.server.learning.controller.admin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.server.learning.pojo.response.UserLearningRecordResponse;
import com.rauio.smartdangjian.server.learning.service.UserLearningRecordService;

@ExtendWith(MockitoExtension.class)
class AdminLearningRecordControllerTest {

    @Mock
    private UserLearningRecordService recordService;

    @InjectMocks
    private AdminLearningRecordController controller;

    @Test
    @DisplayName("getByChapterId 委托 service 获取章节学习记录")
    void getByChapterId() {
        when(recordService.getByChapterId(1L))
                .thenReturn(
                        List.of(UserLearningRecordResponse.builder().id(1L).build()));

        var result = controller.getByChapterId(1L);

        assertThat(result.getData()).hasSize(1);
    }

    @Test
    @DisplayName("delete 委托 service 删除学习记录")
    void delete() {
        when(recordService.delete(1L)).thenReturn(true);

        var result = controller.delete(1L);

        assertThat(result.getData()).isTrue();
    }
}
