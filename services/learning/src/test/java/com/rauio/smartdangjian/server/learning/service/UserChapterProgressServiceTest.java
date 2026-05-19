package com.rauio.smartdangjian.server.learning.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.learning.mapper.UserChapterProgressMapper;
import com.rauio.smartdangjian.server.learning.pojo.convertor.UserChapterProgressConvertor;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserChapterProgress;
import com.rauio.smartdangjian.server.learning.pojo.request.UserChapterProgressRequest;
import com.rauio.smartdangjian.server.learning.pojo.response.UserChapterProgressResponse;

@ExtendWith(MockitoExtension.class)
class UserChapterProgressServiceTest {

    @Mock
    private UserChapterProgressMapper mapper;

    @Mock
    private UserChapterProgressConvertor convertor;

    @Spy
    @InjectMocks
    private UserChapterProgressService progressService;

    private static final String PROGRESS_ID = "p-1";
    private static final String USER_ID = "user-1";
    private static final String CHAPTER_ID = "ch-1";

    // ==================== get ====================

    @Test
    @DisplayName("get 根据ID获取进度记录成功")
    void getSuccess() {
        UserChapterProgress entity = UserChapterProgress.builder()
                .id(PROGRESS_ID)
                .userId(USER_ID)
                .chapterId(CHAPTER_ID)
                .progress(50)
                .status("in_progress")
                .build();
        doReturn(entity).when(progressService).getById(PROGRESS_ID);

        UserChapterProgressResponse vo = UserChapterProgressResponse.builder()
                .id(PROGRESS_ID)
                .userId(USER_ID)
                .chapterId(CHAPTER_ID)
                .progress(50)
                .build();
        when(convertor.toResponse(entity)).thenReturn(vo);

        UserChapterProgressResponse result = progressService.get(PROGRESS_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(PROGRESS_ID);
        assertThat(result.getProgress()).isEqualTo(50);
    }

    @Test
    @DisplayName("get 进度记录不存在抛出异常")
    void getNotFound() {
        doReturn(null).when(progressService).getById(PROGRESS_ID);

        assertThatThrownBy(() -> progressService.get(PROGRESS_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("进度记录不存在");
    }

    // ==================== getByUserId ====================

    @Test
    @DisplayName("getByUserId 查询用户所有进度")
    void getByUserId() {
        List<UserChapterProgress> list = List.of(UserChapterProgress.builder()
                .id(PROGRESS_ID)
                .userId(USER_ID)
                .chapterId(CHAPTER_ID)
                .build());
        doReturn(list).when(progressService).list(any(QueryWrapper.class));

        when(convertor.toResponseList(list))
                .thenReturn(List.of(UserChapterProgressResponse.builder()
                        .id(PROGRESS_ID)
                        .userId(USER_ID)
                        .build()));

        List<UserChapterProgressResponse> result = progressService.getByUserId(USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(PROGRESS_ID);
    }

    // ==================== getByChapterId ====================

    @Test
    @DisplayName("getByChapterId 查询章节下所有进度")
    void getByChapterId() {
        List<UserChapterProgress> list = List.of(UserChapterProgress.builder()
                .id(PROGRESS_ID)
                .userId(USER_ID)
                .chapterId(CHAPTER_ID)
                .build());
        doReturn(list).when(progressService).list(any(QueryWrapper.class));
        when(convertor.toResponseList(list))
                .thenReturn(List.of(UserChapterProgressResponse.builder()
                        .id(PROGRESS_ID)
                        .chapterId(CHAPTER_ID)
                        .build()));

        List<UserChapterProgressResponse> result = progressService.getByChapterId(CHAPTER_ID);

        assertThat(result).hasSize(1);
    }

    // ==================== getByUserIdAndChapterId ====================

    @Test
    @DisplayName("getByUserIdAndChapterId 查询用户章节进度")
    void getByUserIdAndChapterIdSuccess() {
        UserChapterProgress entity = UserChapterProgress.builder()
                .id(PROGRESS_ID)
                .userId(USER_ID)
                .chapterId(CHAPTER_ID)
                .build();
        doReturn(entity).when(progressService).getOne(any(QueryWrapper.class));
        when(convertor.toResponse(entity))
                .thenReturn(UserChapterProgressResponse.builder()
                        .id(PROGRESS_ID)
                        .userId(USER_ID)
                        .chapterId(CHAPTER_ID)
                        .build());

        UserChapterProgressResponse result = progressService.getByUserIdAndChapterId(USER_ID, CHAPTER_ID);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(USER_ID);
    }

    @Test
    @DisplayName("getByUserIdAndChapterId 记录不存在抛出异常")
    void getByUserIdAndChapterIdNotFound() {
        doReturn(null).when(progressService).getOne(any(QueryWrapper.class));

        assertThatThrownBy(() -> progressService.getByUserIdAndChapterId(USER_ID, CHAPTER_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("进度记录不存在");
    }

    // ==================== create ====================

    @Test
    @DisplayName("create 创建进度记录成功")
    void createSuccess() {
        UserChapterProgressRequest dto = UserChapterProgressRequest.builder()
                .userId(USER_ID)
                .chapterId(CHAPTER_ID)
                .progress(30)
                .status("in_progress")
                .build();
        doReturn(null).when(progressService).getOne(any(QueryWrapper.class));

        UserChapterProgress entity = UserChapterProgress.builder()
                .userId(USER_ID)
                .chapterId(CHAPTER_ID)
                .progress(30)
                .status("in_progress")
                .build();
        when(convertor.toEntity(dto)).thenReturn(entity);
        doReturn(true).when(progressService).save(any(UserChapterProgress.class));

        Boolean result = progressService.create(dto);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("create 进度已存在抛出异常")
    void createAlreadyExists() {
        UserChapterProgressRequest dto = UserChapterProgressRequest.builder()
                .userId(USER_ID)
                .chapterId(CHAPTER_ID)
                .build();
        doReturn(UserChapterProgress.builder().id("existing").build())
                .when(progressService)
                .getOne(any(QueryWrapper.class));

        assertThatThrownBy(() -> progressService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已存在");
    }

    @Test
    @DisplayName("create 保存失败抛出异常")
    void createSaveFailed() {
        UserChapterProgressRequest dto = UserChapterProgressRequest.builder()
                .userId(USER_ID)
                .chapterId(CHAPTER_ID)
                .progress(30)
                .build();
        doReturn(null).when(progressService).getOne(any(QueryWrapper.class));
        when(convertor.toEntity(dto))
                .thenReturn(UserChapterProgress.builder()
                        .userId(USER_ID)
                        .chapterId(CHAPTER_ID)
                        .progress(30)
                        .build());
        doReturn(false).when(progressService).save(any(UserChapterProgress.class));

        assertThatThrownBy(() -> progressService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("创建进度记录失败");
    }

    // ==================== update ====================

    @Test
    @DisplayName("update 更新进度记录成功")
    void updateSuccess() {
        UserChapterProgressRequest dto = UserChapterProgressRequest.builder()
                .id(PROGRESS_ID)
                .progress(100)
                .build();
        UserChapterProgress existing = UserChapterProgress.builder()
                .id(PROGRESS_ID)
                .userId(USER_ID)
                .chapterId(CHAPTER_ID)
                .progress(50)
                .status("in_progress")
                .build();
        doReturn(existing).when(progressService).getById(PROGRESS_ID);
        when(convertor.toEntity(dto))
                .thenReturn(UserChapterProgress.builder()
                        .id(PROGRESS_ID)
                        .progress(100)
                        .build());
        doReturn(true).when(progressService).updateById(any(UserChapterProgress.class));

        Boolean result = progressService.update(dto);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("update 没有ID抛出异常")
    void updateIdRequired() {
        UserChapterProgressRequest dto = UserChapterProgressRequest.builder().build();

        assertThatThrownBy(() -> progressService.update(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("进度ID");
    }

    @Test
    @DisplayName("update 记录不存在抛出异常")
    void updateNotFound() {
        UserChapterProgressRequest dto =
                UserChapterProgressRequest.builder().id(PROGRESS_ID).build();
        doReturn(null).when(progressService).getById(PROGRESS_ID);

        assertThatThrownBy(() -> progressService.update(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("进度记录不存在");
    }

    @Test
    @DisplayName("update 进度100时自动设为完成")
    void updateAutoComplete() {
        UserChapterProgressRequest dto = UserChapterProgressRequest.builder()
                .id(PROGRESS_ID)
                .progress(100)
                .build();
        UserChapterProgress existing = UserChapterProgress.builder()
                .id(PROGRESS_ID)
                .userId(USER_ID)
                .chapterId(CHAPTER_ID)
                .progress(50)
                .build();
        doReturn(existing).when(progressService).getById(PROGRESS_ID);

        UserChapterProgress converted =
                UserChapterProgress.builder().id(PROGRESS_ID).progress(100).build();
        when(convertor.toEntity(dto)).thenReturn(converted);
        doReturn(true).when(progressService).updateById(any(UserChapterProgress.class));

        progressService.update(dto);

        verify(progressService)
                .updateById(argThat(entity ->
                        entity.getStatus() != null && entity.getStatus().equals("completed")));
    }

    // ==================== delete ====================

    @Test
    @DisplayName("delete 删除进度记录成功")
    void deleteSuccess() {
        doReturn(UserChapterProgress.builder().id(PROGRESS_ID).build())
                .when(progressService)
                .getById(PROGRESS_ID);
        doReturn(true).when(progressService).removeById(PROGRESS_ID);

        Boolean result = progressService.delete(PROGRESS_ID);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("delete 记录不存在抛出异常")
    void deleteNotFound() {
        doReturn(null).when(progressService).getById(PROGRESS_ID);

        assertThatThrownBy(() -> progressService.delete(PROGRESS_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("进度记录不存在");
    }

    @Test
    @DisplayName("delete 删除失败抛出异常")
    void deleteFailed() {
        doReturn(UserChapterProgress.builder().id(PROGRESS_ID).build())
                .when(progressService)
                .getById(PROGRESS_ID);
        doReturn(false).when(progressService).removeById(PROGRESS_ID);

        assertThatThrownBy(() -> progressService.delete(PROGRESS_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("删除进度记录失败");
    }
}
