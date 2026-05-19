package com.rauio.smartdangjian.server.learning.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.graph.service.KnowledgeGraphService;
import com.rauio.smartdangjian.server.learning.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.server.learning.pojo.convertor.UserLearningRecordConvertor;
import com.rauio.smartdangjian.server.learning.pojo.dto.UserLearningRecordDto;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.learning.pojo.vo.UserLearningRecordVO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserLearningRecordServiceTest {

    @Mock
    private UserLearningRecordMapper mapper;

    @Mock
    private UserLearningRecordConvertor convertor;

    @Mock
    private KnowledgeGraphService knowledgeGraphService;

    @Spy
    @InjectMocks
    private UserLearningRecordService recordService;

    private static final String RECORD_ID = "r-1";
    private static final String USER_ID = "user-1";
    private static final String CHAPTER_ID = "ch-1";
    private static final String COURSE_ID = "course-1";

    // ==================== get ====================

    @Test
    @DisplayName("get 根据ID获取学习记录成功")
    void getSuccess() {
        UserLearningRecord entity = UserLearningRecord.builder()
                .id(RECORD_ID).userId(USER_ID).chapterId(CHAPTER_ID).duration(1800).build();
        doReturn(entity).when(recordService).getById(RECORD_ID);

        UserLearningRecordVO vo = UserLearningRecordVO.builder()
                .id(RECORD_ID).userId(USER_ID).chapterId(CHAPTER_ID).duration(1800).build();
        when(convertor.toVO(entity)).thenReturn(vo);

        UserLearningRecordVO result = recordService.get(RECORD_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(RECORD_ID);
        assertThat(result.getDuration()).isEqualTo(1800);
    }

    @Test
    @DisplayName("get 记录不存在抛出异常")
    void getNotFound() {
        doReturn(null).when(recordService).getById(RECORD_ID);

        assertThatThrownBy(() -> recordService.get(RECORD_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("学习记录不存在");
    }

    // ==================== getPage ====================

    @Test
    @DisplayName("getPage 分页查询学习记录")
    void getPage() {
        UserLearningRecordDto dto = UserLearningRecordDto.builder()
                .userId(USER_ID).deviceType("web").build();
        Page<UserLearningRecord> pageResult = new Page<>(1, 10);
        pageResult.setRecords(List.of(UserLearningRecord.builder().id(RECORD_ID).build()));
        doReturn(pageResult).when(recordService).page(any(Page.class), any(LambdaQueryWrapper.class));

        Page<UserLearningRecord> result = recordService.getPage(dto, 1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(1);
    }

    // ==================== getByUserId ====================

    @Test
    @DisplayName("getByUserId 查询用户所有学习记录")
    void getByUserId() {
        List<UserLearningRecord> list = List.of(
                UserLearningRecord.builder().id(RECORD_ID).userId(USER_ID).chapterId(CHAPTER_ID).build()
        );
        doReturn(list).when(recordService).list(any(QueryWrapper.class));
        when(convertor.toVOList(list)).thenReturn(List.of(
                UserLearningRecordVO.builder().id(RECORD_ID).userId(USER_ID).build()
        ));

        List<UserLearningRecordVO> result = recordService.getByUserId(USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(RECORD_ID);
    }

    // ==================== getRecentByUserId ====================

    @Test
    @DisplayName("getRecentByUserId 查询最近N天学习记录")
    void getRecentByUserId() {
        List<UserLearningRecord> list = List.of(
                UserLearningRecord.builder().id(RECORD_ID).userId(USER_ID).build()
        );
        doReturn(list).when(recordService).list(any(LambdaQueryWrapper.class));

        List<UserLearningRecord> result = recordService.getRecentByUserId(USER_ID, 7);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getRecentByUserId 天数默认为7天")
    void getRecentByUserIdDefaultDays() {
        doReturn(List.of()).when(recordService).list(any(LambdaQueryWrapper.class));

        List<UserLearningRecord> result = recordService.getRecentByUserId(USER_ID, null);

        assertThat(result).isEmpty();
    }

    // ==================== getByChapterId ====================

    @Test
    @DisplayName("getByChapterId 查询章节下所有学习记录")
    void getByChapterId() {
        List<UserLearningRecord> list = List.of(
                UserLearningRecord.builder().id(RECORD_ID).chapterId(CHAPTER_ID).build()
        );
        doReturn(list).when(recordService).list(any(QueryWrapper.class));
        when(convertor.toVOList(list)).thenReturn(List.of(
                UserLearningRecordVO.builder().id(RECORD_ID).chapterId(CHAPTER_ID).build()
        ));

        List<UserLearningRecordVO> result = recordService.getByChapterId(CHAPTER_ID);

        assertThat(result).hasSize(1);
    }

    // ==================== getByUserIdAndChapterId ====================

    @Test
    @DisplayName("getByUserIdAndChapterId 查询用户章节学习记录")
    void getByUserIdAndChapterId() {
        List<UserLearningRecord> list = List.of(
                UserLearningRecord.builder().id(RECORD_ID).userId(USER_ID).chapterId(CHAPTER_ID).build()
        );
        doReturn(list).when(recordService).list(any(QueryWrapper.class));
        when(convertor.toVOList(list)).thenReturn(List.of(
                UserLearningRecordVO.builder().id(RECORD_ID).userId(USER_ID).chapterId(CHAPTER_ID).build()
        ));

        List<UserLearningRecordVO> result = recordService.getByUserIdAndChapterId(USER_ID, CHAPTER_ID);

        assertThat(result).hasSize(1);
    }

    // ==================== getByUserIdAndCourseId ====================

    @Test
    @DisplayName("getByUserIdAndCourseId 查询用户课程学习记录")
    void getByUserIdAndCourseId() {
        List<UserLearningRecord> list = List.of(
                UserLearningRecord.builder().id(RECORD_ID).userId(USER_ID).chapterId(CHAPTER_ID).build()
        );
        doReturn(list).when(recordService).list(any(QueryWrapper.class));

        List<UserLearningRecord> result = recordService.getByUserIdAndCourseId(USER_ID, COURSE_ID);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getByUserIdAndCourseId courseId为空返回空列表")
    void getByUserIdAndCourseIdBlankCourseId() {
        List<UserLearningRecord> result = recordService.getByUserIdAndCourseId(USER_ID, "");

        assertThat(result).isEmpty();
    }

    // ==================== getByUserIdAndCourseIdAndChapterId ====================

    @Test
    @DisplayName("getByUserIdAndCourseIdAndChapterId 查询用户课程章节学习记录")
    void getByUserIdAndCourseIdAndChapterId() {
        List<UserLearningRecord> list = List.of(
                UserLearningRecord.builder().id(RECORD_ID).userId(USER_ID).chapterId(CHAPTER_ID).build()
        );
        doReturn(list).when(recordService).list(any(QueryWrapper.class));

        List<UserLearningRecord> result = recordService.getByUserIdAndCourseIdAndChapterId(USER_ID, COURSE_ID, CHAPTER_ID);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getByUserIdAndCourseIdAndChapterId 参数为空返回空列表")
    void getByUserIdAndCourseIdAndChapterIdBlankParams() {
        assertThat(recordService.getByUserIdAndCourseIdAndChapterId(USER_ID, "", CHAPTER_ID)).isEmpty();
        assertThat(recordService.getByUserIdAndCourseIdAndChapterId(USER_ID, COURSE_ID, "")).isEmpty();
    }

    // ==================== syncUserLearningGraph ====================

    @Test
    @DisplayName("syncUserLearningGraph 同步用户学习图谱")
    void syncUserLearningGraph() {
        List<UserLearningRecord> records = List.of(
                UserLearningRecord.builder().id(RECORD_ID).userId(USER_ID).chapterId(CHAPTER_ID).build(),
                UserLearningRecord.builder().id("r-2").userId(USER_ID).chapterId("ch-2").build()
        );
        doReturn(records).when(recordService).list(any(QueryWrapper.class));

        int result = recordService.syncUserLearningGraph(USER_ID);

        assertThat(result).isEqualTo(2);
        verify(knowledgeGraphService, times(2)).upsertLearningGraph(anyString(), anyString());
    }

    // ==================== create ====================

    @Test
    @DisplayName("create 创建学习记录并同步图谱")
    void createSuccess() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 1, 11, 0);
        UserLearningRecordDto dto = UserLearningRecordDto.builder()
                .userId(USER_ID).chapterId(CHAPTER_ID).startTime(start).endTime(end).build();

        UserLearningRecord entity = UserLearningRecord.builder()
                .userId(USER_ID).chapterId(CHAPTER_ID).startTime(start).endTime(end).build();
        when(convertor.toEntity(dto)).thenReturn(entity);
        doReturn(true).when(recordService).save(any(UserLearningRecord.class));

        Boolean result = recordService.create(dto);

        assertThat(result).isTrue();
        assertThat(entity.getDuration()).isEqualTo(3600);
        verify(knowledgeGraphService).upsertLearningGraph(USER_ID, CHAPTER_ID);
    }

    @Test
    @DisplayName("create 未提供时间时自动设置创建时间")
    void createSetsCreatedAt() {
        UserLearningRecordDto dto = UserLearningRecordDto.builder()
                .userId(USER_ID).chapterId(CHAPTER_ID).build();
        UserLearningRecord entity = UserLearningRecord.builder()
                .userId(USER_ID).chapterId(CHAPTER_ID).build();
        when(convertor.toEntity(dto)).thenReturn(entity);
        doReturn(true).when(recordService).save(any(UserLearningRecord.class));

        recordService.create(dto);

        assertThat(entity.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("create 保存失败抛出异常")
    void createFailed() {
        UserLearningRecordDto dto = UserLearningRecordDto.builder()
                .userId(USER_ID).chapterId(CHAPTER_ID).build();
        when(convertor.toEntity(dto)).thenReturn(UserLearningRecord.builder().build());
        doReturn(false).when(recordService).save(any(UserLearningRecord.class));

        assertThatThrownBy(() -> recordService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("创建学习记录失败");
    }

    // ==================== update ====================

    @Test
    @DisplayName("update 更新学习记录成功")
    void updateSuccess() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 1, 11, 30);
        UserLearningRecordDto dto = UserLearningRecordDto.builder()
                .id(RECORD_ID).startTime(start).endTime(end).build();
        doReturn(UserLearningRecord.builder().id(RECORD_ID).build()).when(recordService).getById(RECORD_ID);

        UserLearningRecord entity = UserLearningRecord.builder()
                .id(RECORD_ID).startTime(start).endTime(end).build();
        when(convertor.toEntity(dto)).thenReturn(entity);
        doReturn(true).when(recordService).updateById(any(UserLearningRecord.class));

        Boolean result = recordService.update(dto);

        assertThat(result).isTrue();
        assertThat(entity.getDuration()).isEqualTo(5400);
    }

    @Test
    @DisplayName("update 没有ID抛出异常")
    void updateIdRequired() {
        UserLearningRecordDto dto = UserLearningRecordDto.builder().build();

        assertThatThrownBy(() -> recordService.update(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("记录ID");
    }

    @Test
    @DisplayName("update 记录不存在抛出异常")
    void updateNotFound() {
        UserLearningRecordDto dto = UserLearningRecordDto.builder().id(RECORD_ID).build();
        doReturn(null).when(recordService).getById(RECORD_ID);

        assertThatThrownBy(() -> recordService.update(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("学习记录不存在");
    }

    // ==================== delete ====================

    @Test
    @DisplayName("delete 删除学习记录成功")
    void deleteSuccess() {
        doReturn(UserLearningRecord.builder().id(RECORD_ID).build()).when(recordService).getById(RECORD_ID);
        doReturn(true).when(recordService).removeById(RECORD_ID);

        Boolean result = recordService.delete(RECORD_ID);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("delete 记录不存在抛出异常")
    void deleteNotFound() {
        doReturn(null).when(recordService).getById(RECORD_ID);

        assertThatThrownBy(() -> recordService.delete(RECORD_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("学习记录不存在");
    }

    @Test
    @DisplayName("delete 删除失败抛出异常")
    void deleteFailed() {
        doReturn(UserLearningRecord.builder().id(RECORD_ID).build()).when(recordService).getById(RECORD_ID);
        doReturn(false).when(recordService).removeById(RECORD_ID);

        assertThatThrownBy(() -> recordService.delete(RECORD_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("删除学习记录失败");
    }
}
