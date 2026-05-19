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

import com.rauio.smartdangjian.server.learning.pojo.dto.UserLearningRecordDto;
import com.rauio.smartdangjian.server.learning.pojo.vo.UserLearningRecordVO;
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
        UserLearningRecordVO vo = UserLearningRecordVO.builder().id("r-1").build();
        when(recordService.get("r-1")).thenReturn(vo);

        var result = controller.get("r-1");

        assertThat(result.getData().getId()).isEqualTo("r-1");
    }

    @Test
    @DisplayName("getByUserId 委托 service 获取用户所有记录")
    void getByUserId() {
        when(recordService.getByUserId("user-1"))
                .thenReturn(List.of(UserLearningRecordVO.builder().id("r-1").build()));

        var result = controller.getByUserId("user-1");

        assertThat(result.getData()).hasSize(1);
    }

    @Test
    @DisplayName("getByUserIdAndChapterId 委托 service 获取用户章节记录")
    void getByUserIdAndChapterId() {
        when(recordService.getByUserIdAndChapterId("user-1", "ch-1"))
                .thenReturn(List.of(UserLearningRecordVO.builder().id("r-1").build()));

        var result = controller.getByUserIdAndChapterId("user-1", "ch-1");

        assertThat(result.getData()).hasSize(1);
    }

    @Test
    @DisplayName("create 委托 service 创建记录")
    void create() {
        UserLearningRecordDto dto = UserLearningRecordDto.builder()
                .userId("user-1")
                .chapterId("ch-1")
                .build();
        when(recordService.create(dto)).thenReturn(true);

        var result = controller.create(dto);

        assertThat(result.getData()).isTrue();
    }

    @Test
    @DisplayName("update 委托 service 更新记录")
    void update() {
        UserLearningRecordDto dto = UserLearningRecordDto.builder().id("r-1").build();
        when(recordService.update(dto)).thenReturn(true);

        var result = controller.update(dto);

        assertThat(result.getData()).isTrue();
    }
}
