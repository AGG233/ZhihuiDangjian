package com.rauio.smartdangjian.server.learning.controller.user;

import com.rauio.smartdangjian.server.learning.pojo.dto.UserChapterProgressDto;
import com.rauio.smartdangjian.server.learning.pojo.vo.UserChapterProgressVO;
import com.rauio.smartdangjian.server.learning.service.UserChapterProgressService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserChapterProgressControllerTest {

    @Mock
    private UserChapterProgressService progressService;

    @InjectMocks
    private UserChapterProgressController controller;

    @Test
    @DisplayName("get 委托 service 获取进度记录")
    void get() {
        UserChapterProgressVO vo = UserChapterProgressVO.builder().id("p-1").build();
        when(progressService.get("p-1")).thenReturn(vo);

        var result = controller.get("p-1");

        assertThat(result.getData().getId()).isEqualTo("p-1");
    }

    @Test
    @DisplayName("getByUserId 委托 service 获取用户所有进度")
    void getByUserId() {
        when(progressService.getByUserId("user-1")).thenReturn(List.of(
                UserChapterProgressVO.builder().id("p-1").build()
        ));

        var result = controller.getByUserId("user-1");

        assertThat(result.getData()).hasSize(1);
    }

    @Test
    @DisplayName("getByUserIdAndChapterId 委托 service 获取用户章节进度")
    void getByUserIdAndChapterId() {
        UserChapterProgressVO vo = UserChapterProgressVO.builder().id("p-1").build();
        when(progressService.getByUserIdAndChapterId("user-1", "ch-1")).thenReturn(vo);

        var result = controller.getByUserIdAndChapterId("user-1", "ch-1");

        assertThat(result.getData()).isNotNull();
    }

    @Test
    @DisplayName("create 委托 service 创建进度")
    void create() {
        UserChapterProgressDto dto = UserChapterProgressDto.builder().userId("user-1").chapterId("ch-1").build();
        when(progressService.create(dto)).thenReturn(true);

        var result = controller.create(dto);

        assertThat(result.getData()).isTrue();
    }

    @Test
    @DisplayName("update 委托 service 更新进度")
    void update() {
        UserChapterProgressDto dto = UserChapterProgressDto.builder().id("p-1").progress(80).build();
        when(progressService.update(dto)).thenReturn(true);

        var result = controller.update(dto);

        assertThat(result.getData()).isTrue();
    }
}
