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

import com.rauio.smartdangjian.server.learning.pojo.vo.UserChapterProgressVO;
import com.rauio.smartdangjian.server.learning.service.UserChapterProgressService;

@ExtendWith(MockitoExtension.class)
class AdminChapterProgressControllerTest {

    @Mock
    private UserChapterProgressService progressService;

    @InjectMocks
    private AdminChapterProgressController controller;

    @Test
    @DisplayName("getByChapterId 委托 service 获取章节进度")
    void getByChapterId() {
        when(progressService.getByChapterId("ch-1"))
                .thenReturn(List.of(UserChapterProgressVO.builder().id("p-1").build()));

        var result = controller.getByChapterId("ch-1");

        assertThat(result.getData()).hasSize(1);
    }

    @Test
    @DisplayName("delete 委托 service 删除进度")
    void delete() {
        when(progressService.delete("p-1")).thenReturn(true);

        var result = controller.delete("p-1");

        assertThat(result.getData()).isTrue();
    }
}
