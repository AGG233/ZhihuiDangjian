package com.rauio.smartdangjian.server.learning.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.graph.service.KnowledgeGraphService;
import com.rauio.smartdangjian.server.learning.mapper.UserLearningRecordMapper;
import com.rauio.smartdangjian.server.learning.pojo.convertor.UserLearningRecordConvertor;
import com.rauio.smartdangjian.server.learning.pojo.entity.UserLearningRecord;
import com.rauio.smartdangjian.server.learning.pojo.request.UserLearningRecordRequest;
import com.rauio.smartdangjian.server.learning.pojo.response.UserLearningRecordResponse;

@ExtendWith(MockitoExtension.class)
class UserLearningRecordServiceTest {

    @Mock
    private UserLearningRecordMapper mapper;

    @Mock
    private UserLearningRecordConvertor convertor;

    @Mock
    private KnowledgeGraphService knowledgeGraphService;

    private UserLearningRecordService recordService;

    @BeforeEach
    void resetSpy() {
        recordService = spy(new UserLearningRecordService(
                convertor,
                knowledgeGraphService,
                Clock.fixed(Instant.parse("2026-05-31T10:15:30Z"), ZoneId.of("UTC"))));
    }

    private static final Long RECORD_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long CHAPTER_ID = 1L;
    private static final Long COURSE_ID = 1L;

    @Test
    @DisplayName("事务边界按方法声明：读方法只读，写方法显式回滚")
    void transactionalBoundariesAreMethodLevel() throws NoSuchMethodException {
        assertThat(UserLearningRecordService.class.getAnnotation(Transactional.class))
                .isNull();
        assertReadOnlyTransaction("get", Long.class);
        assertReadOnlyTransaction("getPage", UserLearningRecordRequest.class, int.class, int.class);
        assertReadOnlyTransaction("getByUserId", Long.class);
        assertReadOnlyTransaction("getRecentByUserId", String.class, Integer.class);
        assertReadOnlyTransaction("getByChapterId", Long.class);
        assertReadOnlyTransaction("getByUserIdAndChapterId", Long.class, Long.class);
        assertReadOnlyTransaction("getByUserIdAndCourseId", Long.class, Long.class);
        assertReadOnlyTransaction("getByUserIdAndCourseIdAndChapterId", Long.class, Long.class, Long.class);
        assertWriteTransaction("syncUserLearningGraph", Long.class);
        assertWriteTransaction("create", UserLearningRecordRequest.class);
        assertWriteTransaction("update", UserLearningRecordRequest.class);
        assertWriteTransaction("delete", Long.class);
    }

    private void assertReadOnlyTransaction(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = UserLearningRecordService.class.getMethod(methodName, parameterTypes);
        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.readOnly()).isTrue();
    }

    private void assertWriteTransaction(String methodName, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = UserLearningRecordService.class.getMethod(methodName, parameterTypes);
        Transactional transactional = method.getAnnotation(Transactional.class);

        assertThat(transactional).isNotNull();
        assertThat(transactional.readOnly()).isFalse();
        assertThat(transactional.rollbackFor()).contains(Exception.class);
    }

    // ==================== get ====================

    @Test
    @DisplayName("get 根据ID获取学习记录成功")
    void getSuccess() {
        UserLearningRecord entity = UserLearningRecord.builder()
                .id(RECORD_ID)
                .userId(USER_ID)
                .chapterId(null)
                .duration(1800)
                .build();
        doReturn(entity).when(recordService).getById(RECORD_ID);

        UserLearningRecordResponse vo = UserLearningRecordResponse.builder()
                .id(RECORD_ID)
                .userId(USER_ID)
                .chapterId(null)
                .duration(1800)
                .build();
        when(convertor.toResponse(entity)).thenReturn(vo);

        UserLearningRecordResponse result = recordService.get(RECORD_ID);

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
        UserLearningRecordRequest dto = UserLearningRecordRequest.builder()
                .userId(USER_ID)
                .deviceType("web")
                .build();
        Page<UserLearningRecord> pageResult = new Page<>(1, 10);
        pageResult.setRecords(List.of(UserLearningRecord.builder().id(RECORD_ID).build()));
        doReturn(pageResult).when(recordService).page(any(Page.class), any(LambdaQueryWrapper.class));

        Page<UserLearningRecord> result = recordService.getPage(dto, 1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    @DisplayName("getPage 所有条件为空时也能正常查询")
    void getPageWithNullConditions() {
        UserLearningRecordRequest dto = UserLearningRecordRequest.builder().build();
        Page<UserLearningRecord> pageResult = new Page<>(1, 10);
        doReturn(pageResult).when(recordService).page(any(Page.class), any(LambdaQueryWrapper.class));

        Page<UserLearningRecord> result = recordService.getPage(dto, 1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getRecords()).isEmpty();
    }

    // ==================== getByUserId ====================

    @Test
    @DisplayName("getByUserId 查询用户所有学习记录")
    void getByUserId() {
        List<UserLearningRecord> list = List.of(UserLearningRecord.builder()
                .id(RECORD_ID)
                .userId(USER_ID)
                .chapterId(null)
                .build());
        doReturn(list).when(recordService).list(any(QueryWrapper.class));
        when(convertor.toResponseList(list))
                .thenReturn(List.of(UserLearningRecordResponse.builder()
                        .id(RECORD_ID)
                        .userId(USER_ID)
                        .build()));

        List<UserLearningRecordResponse> result = recordService.getByUserId(USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(RECORD_ID);
    }

    // ==================== getRecentByUserId ====================

    @Test
    @DisplayName("getRecentByUserId 查询最近N天学习记录")
    void getRecentByUserId() {
        List<UserLearningRecord> list = List.of(
                UserLearningRecord.builder().id(RECORD_ID).userId(USER_ID).build());
        doReturn(list).when(recordService).list(any(LambdaQueryWrapper.class));

        List<UserLearningRecord> result = recordService.getRecentByUserId("1", 7);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("getRecentByUserId 天数默认为7天")
    void getRecentByUserIdDefaultDays() {
        doReturn(List.of()).when(recordService).list(any(LambdaQueryWrapper.class));

        List<UserLearningRecord> result = recordService.getRecentByUserId("1", null);

        assertThat(result).isEmpty();
    }

    @ParameterizedTest(name = "days={0}")
    @NullSource
    @ValueSource(ints = {0, -1, -30})
    @DisplayName("getRecentByUserId 天数为空或小于等于0时默认为7天")
    void getRecentByUserIdNonPositiveDays(Integer days) {
        doReturn(List.of()).when(recordService).list(any(LambdaQueryWrapper.class));

        List<UserLearningRecord> result = recordService.getRecentByUserId("1", days);

        assertThat(result).isEmpty();
    }

    @ParameterizedTest(name = "userId=''{0}''")
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t"})
    @DisplayName("getRecentByUserId 用户 ID 为空白时仍按条件查询并返回查询结果")
    void getRecentByUserIdBlankUserId(String userId) {
        doReturn(List.of()).when(recordService).list(any(LambdaQueryWrapper.class));

        List<UserLearningRecord> result = recordService.getRecentByUserId(userId, 7);

        assertThat(result).isEmpty();
        verify(recordService).list(any(LambdaQueryWrapper.class));
    }

    // ==================== getByChapterId ====================

    @Test
    @DisplayName("getByChapterId 查询章节下所有学习记录")
    void getByChapterId() {
        List<UserLearningRecord> list = List.of(
                UserLearningRecord.builder().id(RECORD_ID).chapterId(null).build());
        doReturn(list).when(recordService).list(any(QueryWrapper.class));
        when(convertor.toResponseList(list))
                .thenReturn(List.of(UserLearningRecordResponse.builder()
                        .id(RECORD_ID)
                        .chapterId(null)
                        .build()));

        List<UserLearningRecordResponse> result = recordService.getByChapterId(CHAPTER_ID);

        assertThat(result).hasSize(1);
    }

    // ==================== getByUserIdAndChapterId ====================

    @Test
    @DisplayName("getByUserIdAndChapterId 查询用户章节学习记录")
    void getByUserIdAndChapterId() {
        List<UserLearningRecord> list = List.of(UserLearningRecord.builder()
                .id(RECORD_ID)
                .userId(USER_ID)
                .chapterId(null)
                .build());
        doReturn(list).when(recordService).list(any(QueryWrapper.class));
        when(convertor.toResponseList(list))
                .thenReturn(List.of(UserLearningRecordResponse.builder()
                        .id(RECORD_ID)
                        .userId(USER_ID)
                        .chapterId(null)
                        .build()));

        List<UserLearningRecordResponse> result = recordService.getByUserIdAndChapterId(USER_ID, CHAPTER_ID);

        assertThat(result).hasSize(1);
    }

    // ==================== getByUserIdAndCourseId ====================

    @Test
    @DisplayName("getByUserIdAndCourseId 查询用户课程学习记录")
    void getByUserIdAndCourseId() {
        List<UserLearningRecord> list = List.of(UserLearningRecord.builder()
                .id(RECORD_ID)
                .userId(USER_ID)
                .chapterId(null)
                .build());
        doReturn(list).when(recordService).list(any(QueryWrapper.class));

        List<UserLearningRecord> result = recordService.getByUserIdAndCourseId(USER_ID, COURSE_ID);

        assertThat(result).hasSize(1);
        ArgumentCaptor<QueryWrapper<UserLearningRecord>> wrapperCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(recordService).list(wrapperCaptor.capture());
        String sqlSegment = wrapperCaptor.getValue().getSqlSegment();
        assertThat(sqlSegment).contains("course_id = #{");
        assertThat(sqlSegment).doesNotContain("course_id = " + COURSE_ID);
    }

    @Test
    @DisplayName("getByUserIdAndCourseId courseId为空返回空列表")
    void getByUserIdAndCourseIdBlankCourseId() {
        List<UserLearningRecord> result = recordService.getByUserIdAndCourseId(USER_ID, null);

        assertThat(result).isEmpty();
    }

    // ==================== getByUserIdAndCourseIdAndChapterId ====================

    @Test
    @DisplayName("getByUserIdAndCourseIdAndChapterId 查询用户课程章节学习记录")
    void getByUserIdAndCourseIdAndChapterId() {
        List<UserLearningRecord> list = List.of(UserLearningRecord.builder()
                .id(RECORD_ID)
                .userId(USER_ID)
                .chapterId(null)
                .build());
        doReturn(list).when(recordService).list(any(QueryWrapper.class));

        List<UserLearningRecord> result =
                recordService.getByUserIdAndCourseIdAndChapterId(USER_ID, COURSE_ID, CHAPTER_ID);

        assertThat(result).hasSize(1);
        ArgumentCaptor<QueryWrapper<UserLearningRecord>> wrapperCaptor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(recordService).list(wrapperCaptor.capture());
        String sqlSegment = wrapperCaptor.getValue().getSqlSegment();
        assertThat(sqlSegment).contains("course_id = #{");
        assertThat(sqlSegment).doesNotContain("course_id = " + COURSE_ID);
    }

    @Test
    @DisplayName("getByUserIdAndCourseIdAndChapterId 参数为空返回空列表")
    void getByUserIdAndCourseIdAndChapterIdBlankParams() {
        assertThat(recordService.getByUserIdAndCourseIdAndChapterId(USER_ID, null, CHAPTER_ID))
                .isEmpty();
        assertThat(recordService.getByUserIdAndCourseIdAndChapterId(USER_ID, COURSE_ID, null))
                .isEmpty();
    }

    // ==================== syncUserLearningGraph ====================

    @Test
    @DisplayName("syncUserLearningGraph 同步用户学习图谱")
    void syncUserLearningGraph() {
        List<UserLearningRecord> records = List.of(
                UserLearningRecord.builder()
                        .id(RECORD_ID)
                        .userId(USER_ID)
                        .chapterId(null)
                        .build(),
                UserLearningRecord.builder()
                        .id(1L)
                        .userId(USER_ID)
                        .chapterId(1L)
                        .build());
        doReturn(records)
                .when(recordService)
                .list(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class));

        int result = recordService.syncUserLearningGraph(USER_ID);

        assertThat(result).isEqualTo(2);
        verify(knowledgeGraphService).batchUpsertLearningGraph(eq(USER_ID), any());
    }

    @Test
    @DisplayName("syncUserLearningGraph 所有 chapterId 为 null 时跳过图谱同步")
    void syncUserLearningGraphSkipsNullFields() {
        List<UserLearningRecord> records = List.of(
                UserLearningRecord.builder().id(1L).userId(null).chapterId(null).build(),
                UserLearningRecord.builder()
                        .id(2L)
                        .userId(USER_ID)
                        .chapterId(null)
                        .build());
        doReturn(records)
                .when(recordService)
                .list(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class));

        int result = recordService.syncUserLearningGraph(USER_ID);

        assertThat(result).isZero();
        verify(knowledgeGraphService, never()).upsertLearningGraph(anyLong(), anyLong());
    }

    // ==================== create ====================

    @Test
    @DisplayName("create 创建学习记录并同步图谱")
    void createSuccess() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 1, 11, 0);
        UserLearningRecordRequest dto = UserLearningRecordRequest.builder()
                .userId(USER_ID)
                .chapterId(CHAPTER_ID)
                .startTime(start)
                .endTime(end)
                .build();

        UserLearningRecord entity = UserLearningRecord.builder()
                .userId(USER_ID)
                .chapterId(CHAPTER_ID)
                .startTime(start)
                .endTime(end)
                .build();
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
        UserLearningRecordRequest dto = UserLearningRecordRequest.builder()
                .userId(USER_ID)
                .chapterId(null)
                .build();
        UserLearningRecord entity =
                UserLearningRecord.builder().userId(USER_ID).chapterId(null).build();
        when(convertor.toEntity(dto)).thenReturn(entity);
        doReturn(true).when(recordService).save(any(UserLearningRecord.class));

        recordService.create(dto);

        assertThat(entity.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("create 保存失败抛出异常")
    void createFailed() {
        UserLearningRecordRequest dto = UserLearningRecordRequest.builder()
                .userId(USER_ID)
                .chapterId(null)
                .build();
        when(convertor.toEntity(dto)).thenReturn(UserLearningRecord.builder().build());
        doReturn(false).when(recordService).save(any(UserLearningRecord.class));

        assertThatThrownBy(() -> recordService.create(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("创建学习记录失败");
    }

    @Test
    @DisplayName("create 创建学习记录不提供起止时间时跳过时长计算")
    void createNoTimeRange() {
        UserLearningRecordRequest dto = UserLearningRecordRequest.builder()
                .userId(USER_ID)
                .chapterId(null)
                .build();

        UserLearningRecord entity = UserLearningRecord.builder()
                .userId(USER_ID)
                .chapterId(null)
                .createdAt(LocalDateTime.now())
                .build();
        when(convertor.toEntity(dto)).thenReturn(entity);
        doReturn(true).when(recordService).save(any(UserLearningRecord.class));

        Boolean result = recordService.create(dto);

        assertThat(result).isTrue();
        assertThat(entity.getDuration()).isNull();
    }

    @Test
    @DisplayName("create 学习记录中 userId 为 null 时跳过图谱同步")
    void createNoUserIdSkipsGraphSync() {
        UserLearningRecordRequest dto =
                UserLearningRecordRequest.builder().chapterId(null).build();

        UserLearningRecord entity = UserLearningRecord.builder()
                .chapterId(null)
                .createdAt(LocalDateTime.now())
                .build();
        when(convertor.toEntity(dto)).thenReturn(entity);
        doReturn(true).when(recordService).save(any(UserLearningRecord.class));

        Boolean result = recordService.create(dto);

        assertThat(result).isTrue();
        verify(knowledgeGraphService, never()).upsertLearningGraph(anyLong(), anyLong());
    }

    // ==================== update ====================

    @Test
    @DisplayName("update 更新学习记录成功")
    void updateSuccess() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 1, 11, 30);
        UserLearningRecordRequest dto = UserLearningRecordRequest.builder()
                .id(RECORD_ID)
                .startTime(start)
                .endTime(end)
                .build();
        doReturn(UserLearningRecord.builder().id(RECORD_ID).build())
                .when(recordService)
                .getById(RECORD_ID);

        UserLearningRecord entity = UserLearningRecord.builder()
                .id(RECORD_ID)
                .startTime(start)
                .endTime(end)
                .build();
        when(convertor.toEntity(dto)).thenReturn(entity);
        doReturn(true).when(recordService).updateById(any(UserLearningRecord.class));

        Boolean result = recordService.update(dto);

        assertThat(result).isTrue();
        assertThat(entity.getDuration()).isEqualTo(5400);
    }

    @Test
    @DisplayName("update 没有ID抛出异常")
    void updateIdRequired() {
        UserLearningRecordRequest dto = UserLearningRecordRequest.builder().build();

        assertThatThrownBy(() -> recordService.update(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("记录ID");
    }

    @Test
    @DisplayName("update 记录不存在抛出异常")
    void updateNotFound() {
        UserLearningRecordRequest dto =
                UserLearningRecordRequest.builder().id(RECORD_ID).build();
        doReturn(null).when(recordService).getById(RECORD_ID);

        assertThatThrownBy(() -> recordService.update(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("学习记录不存在");
    }

    @Test
    @DisplayName("update 不提供起止时间时跳过时长计算")
    void updateNoTimeRange() {
        UserLearningRecordRequest dto =
                UserLearningRecordRequest.builder().id(RECORD_ID).build();
        doReturn(UserLearningRecord.builder().id(RECORD_ID).build())
                .when(recordService)
                .getById(RECORD_ID);

        UserLearningRecord entity = UserLearningRecord.builder().id(RECORD_ID).build();
        when(convertor.toEntity(dto)).thenReturn(entity);
        doReturn(true).when(recordService).updateById(any(UserLearningRecord.class));

        Boolean result = recordService.update(dto);

        assertThat(result).isTrue();
        assertThat(entity.getDuration()).isNull();
    }

    // ==================== delete ====================

    @Test
    @DisplayName("delete 删除学习记录成功")
    void deleteSuccess() {
        doReturn(UserLearningRecord.builder().id(RECORD_ID).build())
                .when(recordService)
                .getById(RECORD_ID);
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
        doReturn(UserLearningRecord.builder().id(RECORD_ID).build())
                .when(recordService)
                .getById(RECORD_ID);
        doReturn(false).when(recordService).removeById(RECORD_ID);

        assertThatThrownBy(() -> recordService.delete(RECORD_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("删除学习记录失败");
    }

    // ==================== 缺失分支补充 ====================

    @Test
    @DisplayName("getPage 包含 chapterId 和 createdAt 条件")
    void getPageWithAllConditions() {
        UserLearningRecordRequest dto = UserLearningRecordRequest.builder()
                .userId(USER_ID)
                .chapterId(null)
                .deviceType("web")
                .createdAt(LocalDateTime.now())
                .build();
        Page<UserLearningRecord> pageResult = new Page<>(1, 10);
        pageResult.setRecords(List.of(UserLearningRecord.builder().id(RECORD_ID).build()));
        doReturn(pageResult).when(recordService).page(any(Page.class), any(LambdaQueryWrapper.class));

        Page<UserLearningRecord> result = recordService.getPage(dto, 1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    @DisplayName("getByUserId 无学习记录返回空列表")
    void getByUserIdEmpty() {
        doReturn(List.of()).when(recordService).list(any(QueryWrapper.class));
        when(convertor.toResponseList(List.of())).thenReturn(List.of());

        List<UserLearningRecordResponse> result = recordService.getByUserId(USER_ID);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("update updateById 失败时抛出异常")
    void updateFailed() {
        UserLearningRecordRequest dto =
                UserLearningRecordRequest.builder().id(RECORD_ID).build();
        doReturn(UserLearningRecord.builder().id(RECORD_ID).build())
                .when(recordService)
                .getById(RECORD_ID);
        UserLearningRecord entity = UserLearningRecord.builder().id(RECORD_ID).build();
        when(convertor.toEntity(dto)).thenReturn(entity);
        doReturn(false).when(recordService).updateById(any(UserLearningRecord.class));

        assertThatThrownBy(() -> recordService.update(dto))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("更新学习记录失败");
    }

    @Test
    @DisplayName("create startTime set but endTime null skips duration")
    void createWithStartTimeOnly() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
        UserLearningRecordRequest dto = UserLearningRecordRequest.builder()
                .userId(USER_ID)
                .chapterId(null)
                .startTime(start)
                .build();

        UserLearningRecord entity = UserLearningRecord.builder()
                .userId(USER_ID)
                .chapterId(null)
                .startTime(start)
                .createdAt(LocalDateTime.now())
                .build();
        when(convertor.toEntity(dto)).thenReturn(entity);
        doReturn(true).when(recordService).save(any(UserLearningRecord.class));

        Boolean result = recordService.create(dto);

        assertThat(result).isTrue();
        assertThat(entity.getDuration()).isNull();
    }

    @Test
    @DisplayName("create userId set but chapterId null skips graph sync")
    void createWithUserIdOnlyNoChapterSkipsGraphSync() {
        UserLearningRecordRequest dto =
                UserLearningRecordRequest.builder().userId(USER_ID).build();

        UserLearningRecord entity = UserLearningRecord.builder()
                .userId(USER_ID)
                .createdAt(LocalDateTime.now())
                .build();
        when(convertor.toEntity(dto)).thenReturn(entity);
        doReturn(true).when(recordService).save(any(UserLearningRecord.class));

        Boolean result = recordService.create(dto);

        assertThat(result).isTrue();
        verify(knowledgeGraphService, never()).upsertLearningGraph(anyLong(), anyLong());
    }

    @Test
    @DisplayName("update startTime set but endTime null skips duration")
    void updateWithStartTimeOnly() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 1, 10, 0);
        UserLearningRecordRequest dto = UserLearningRecordRequest.builder()
                .id(RECORD_ID)
                .startTime(start)
                .build();
        doReturn(UserLearningRecord.builder().id(RECORD_ID).build())
                .when(recordService)
                .getById(RECORD_ID);

        UserLearningRecord entity =
                UserLearningRecord.builder().id(RECORD_ID).startTime(start).build();
        when(convertor.toEntity(dto)).thenReturn(entity);
        doReturn(true).when(recordService).updateById(any(UserLearningRecord.class));

        Boolean result = recordService.update(dto);

        assertThat(result).isTrue();
        assertThat(entity.getDuration()).isNull();
    }

    @Test
    @DisplayName("create userId and chapterId both set syncs graph")
    void createWithUserIdAndChapterIdSyncsGraph() {
        UserLearningRecordRequest dto = UserLearningRecordRequest.builder()
                .userId(USER_ID)
                .chapterId(CHAPTER_ID)
                .build();
        UserLearningRecord entity = UserLearningRecord.builder()
                .userId(USER_ID)
                .chapterId(CHAPTER_ID)
                .createdAt(LocalDateTime.now())
                .build();
        when(convertor.toEntity(dto)).thenReturn(entity);
        doReturn(true).when(recordService).save(any(UserLearningRecord.class));

        Boolean result = recordService.create(dto);

        assertThat(result).isTrue();
        verify(knowledgeGraphService).upsertLearningGraph(USER_ID, CHAPTER_ID);
    }

    @Test
    @DisplayName("syncUserLearningGraph 空记录返回 0")
    void syncUserLearningGraphEmpty() {
        doReturn(List.of())
                .when(recordService)
                .list(any(com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper.class));

        int result = recordService.syncUserLearningGraph(USER_ID);

        assertThat(result).isZero();
    }
}
