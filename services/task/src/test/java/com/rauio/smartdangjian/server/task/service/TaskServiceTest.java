package com.rauio.smartdangjian.server.task.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.task.constants.TaskErrorConstants;
import com.rauio.smartdangjian.server.task.mapper.TaskAcceptanceMapper;
import com.rauio.smartdangjian.server.task.mapper.TaskMapper;
import com.rauio.smartdangjian.server.task.pojo.convertor.TaskConvertor;
import com.rauio.smartdangjian.server.task.pojo.entity.Task;
import com.rauio.smartdangjian.server.task.pojo.entity.TaskAcceptance;
import com.rauio.smartdangjian.server.task.pojo.request.TaskSubmitRequest;
import com.rauio.smartdangjian.server.task.pojo.response.TaskAcceptanceResponse;
import com.rauio.smartdangjian.server.task.pojo.response.TaskPageResponse;
import com.rauio.smartdangjian.server.task.pojo.response.TaskResponse;
import com.rauio.smartdangjian.server.task.spec.TaskAcceptanceStatus;
import com.rauio.smartdangjian.server.task.spec.TaskStatus;

import cn.dev33.satoken.stp.StpUtil;

/**
 * 用户端任务服务单元测试：已发布任务分页、领取防重复、提交状态机。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TaskServiceTest {

    private static final Long STUDENT_ID = 100L;
    private static final Long TASK_ID = 1L;

    @BeforeAll
    static void initMybatisPlus() {
        MybatisConfiguration config = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(config, "");
        TableInfoHelper.initTableInfo(assistant, Task.class);
        TableInfoHelper.initTableInfo(assistant, TaskAcceptance.class);
    }

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private TaskConvertor taskConvertor;

    @Mock
    private TaskAcceptanceMapper taskAcceptanceMapper;

    @Spy
    @InjectMocks
    private TaskService taskService;

    private MockedStatic<StpUtil> stpUtilMock;

    @BeforeEach
    void setUp() {
        lenient().doReturn(taskAcceptanceMapper).when(taskService).getBaseMapper();
        lenient().when(taskAcceptanceMapper.insert(any(TaskAcceptance.class))).thenReturn(1);
        lenient()
                .when(taskAcceptanceMapper.updateById(any(TaskAcceptance.class)))
                .thenReturn(1);
        lenient()
                .when(taskConvertor.toAcceptanceResponse(any(TaskAcceptance.class)))
                .thenReturn(TaskAcceptanceResponse.builder()
                        .taskId(TASK_ID)
                        .userId(STUDENT_ID)
                        .build());
        lenient()
                .when(taskConvertor.toResponseList(any()))
                .thenReturn(List.of(TaskResponse.builder().build()));
        mockLoggedIn(STUDENT_ID);
    }

    @AfterEach
    void tearDown() {
        closeMock();
    }

    // ==================== getPublishedPage ====================

    @Test
    @DisplayName("分页查询：仅返回已发布任务，并填充分页信息")
    void getPublishedPageReturnsPublishedTasksOnly() {
        doAnswer(invocation -> {
                    Page<Task> arg = invocation.getArgument(0);
                    arg.setRecords(List.of(Task.builder().id(TASK_ID).build()));
                    arg.setTotal(3);
                    return null;
                })
                .when(taskMapper)
                .selectPage(any(Page.class), any());

        TaskPageResponse result = taskService.getPublishedPage(1, 10);

        assertThat(result.getTotal()).isEqualTo(3);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getRecords()).hasSize(1);
    }

    // ==================== accept ====================

    @Test
    @DisplayName("领取任务：落库 accepted 状态，进度为 0")
    void acceptPersistsAcceptedRecord() {
        when(taskMapper.selectById(TASK_ID)).thenReturn(publishedTask());
        when(taskAcceptanceMapper.selectCount(any())).thenReturn(0L);

        TaskAcceptanceResponse response = taskService.accept(TASK_ID);

        assertThat(response.getTaskId()).isEqualTo(TASK_ID);
        ArgumentCaptor<TaskAcceptance> captor = ArgumentCaptor.forClass(TaskAcceptance.class);
        verify(taskAcceptanceMapper).insert(captor.capture());
        TaskAcceptance saved = captor.getValue();
        assertThat(saved.getTaskId()).isEqualTo(TASK_ID);
        assertThat(saved.getUserId()).isEqualTo(STUDENT_ID);
        assertThat(saved.getProgress()).isZero();
        assertThat(saved.getStatus()).isEqualTo(TaskAcceptanceStatus.ACCEPTED);
        assertThat(saved.getAcceptedAt()).isNotNull();
    }

    @Test
    @DisplayName("领取任务：已领取过抛 9002，不重复落库")
    void acceptDuplicateThrows() {
        when(taskMapper.selectById(TASK_ID)).thenReturn(publishedTask());
        when(taskAcceptanceMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> taskService.accept(TASK_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(TaskErrorConstants.TASK_ALREADY_ACCEPTED));
        verify(taskAcceptanceMapper, never()).insert(any(TaskAcceptance.class));
    }

    @Test
    @DisplayName("领取任务：并发冲突（唯一约束）时抛 TASK_ALREADY_ACCEPTED")
    void acceptDuplicateKeyThrows() {
        when(taskMapper.selectById(TASK_ID)).thenReturn(publishedTask());
        when(taskAcceptanceMapper.selectCount(any())).thenReturn(0L);
        doThrow(new org.springframework.dao.DuplicateKeyException("uk_task_acceptance_task_user"))
                .when(taskAcceptanceMapper)
                .insert(any(TaskAcceptance.class));

        assertThatThrownBy(() -> taskService.accept(TASK_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(TaskErrorConstants.TASK_ALREADY_ACCEPTED));
    }

    @Test
    @DisplayName("领取任务：已关闭任务抛 9003")
    void acceptClosedTaskThrows() {
        when(taskMapper.selectById(TASK_ID))
                .thenReturn(Task.builder().id(TASK_ID).status(TaskStatus.CLOSED).build());

        assertThatThrownBy(() -> taskService.accept(TASK_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(
                        e -> assertThat(((BusinessException) e).getCode()).isEqualTo(TaskErrorConstants.TASK_CLOSED));
        verify(taskAcceptanceMapper, never()).insert(any(TaskAcceptance.class));
    }

    @Test
    @DisplayName("领取任务：未发布任务抛 9004")
    void acceptDraftTaskThrows() {
        when(taskMapper.selectById(TASK_ID))
                .thenReturn(Task.builder().id(TASK_ID).status(TaskStatus.DRAFT).build());

        assertThatThrownBy(() -> taskService.accept(TASK_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e ->
                        assertThat(((BusinessException) e).getCode()).isEqualTo(TaskErrorConstants.TASK_NOT_PUBLISHED));
    }

    @Test
    @DisplayName("领取任务：任务不存在抛 9001")
    void acceptMissingTaskThrows() {
        when(taskMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> taskService.accept(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e ->
                        assertThat(((BusinessException) e).getCode()).isEqualTo(TaskErrorConstants.TASK_NOT_FOUND));
    }

    // ==================== submit ====================

    @Test
    @DisplayName("提交任务进度100：状态置 completed 并记录完成时间")
    void submitFullProgressCompletes() {
        when(taskMapper.selectById(TASK_ID)).thenReturn(publishedTask());
        when(taskAcceptanceMapper.selectOne(any(), anyBoolean()))
                .thenReturn(acceptance(TaskAcceptanceStatus.ACCEPTED, 0));

        taskService.submit(TASK_ID, TaskSubmitRequest.builder().progress(100).build());

        ArgumentCaptor<TaskAcceptance> captor = ArgumentCaptor.forClass(TaskAcceptance.class);
        verify(taskAcceptanceMapper).updateById(captor.capture());
        TaskAcceptance updated = captor.getValue();
        assertThat(updated.getProgress()).isEqualTo(100);
        assertThat(updated.getStatus()).isEqualTo(TaskAcceptanceStatus.COMPLETED);
        assertThat(updated.getCompletedAt()).isNotNull();
    }

    @Test
    @DisplayName("提交任务进度50：状态置 submitted 且保留进度")
    void submitPartialProgressMarksSubmitted() {
        when(taskMapper.selectById(TASK_ID)).thenReturn(publishedTask());
        when(taskAcceptanceMapper.selectOne(any(), anyBoolean()))
                .thenReturn(acceptance(TaskAcceptanceStatus.ACCEPTED, 0));

        taskService.submit(TASK_ID, TaskSubmitRequest.builder().progress(50).build());

        ArgumentCaptor<TaskAcceptance> captor = ArgumentCaptor.forClass(TaskAcceptance.class);
        verify(taskAcceptanceMapper).updateById(captor.capture());
        TaskAcceptance updated = captor.getValue();
        assertThat(updated.getProgress()).isEqualTo(50);
        assertThat(updated.getStatus()).isEqualTo(TaskAcceptanceStatus.SUBMITTED);
        assertThat(updated.getCompletedAt()).isNull();
    }

    @Test
    @DisplayName("提交任务：已提交再提交抛 9006")
    void submitAlreadySubmittedThrows() {
        when(taskMapper.selectById(TASK_ID)).thenReturn(publishedTask());
        when(taskAcceptanceMapper.selectOne(any(), anyBoolean()))
                .thenReturn(acceptance(TaskAcceptanceStatus.SUBMITTED, 60));

        assertThatThrownBy(() -> taskService.submit(
                        TASK_ID, TaskSubmitRequest.builder().progress(90).build()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(TaskErrorConstants.TASK_ALREADY_SUBMITTED));
        verify(taskAcceptanceMapper, never()).updateById(any(TaskAcceptance.class));
    }

    @Test
    @DisplayName("提交任务：已完成再提交抛 9006")
    void submitCompletedThrows() {
        when(taskMapper.selectById(TASK_ID)).thenReturn(publishedTask());
        when(taskAcceptanceMapper.selectOne(any(), anyBoolean()))
                .thenReturn(acceptance(TaskAcceptanceStatus.COMPLETED, 100));

        assertThatThrownBy(() -> taskService.submit(
                        TASK_ID, TaskSubmitRequest.builder().progress(100).build()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(TaskErrorConstants.TASK_ALREADY_SUBMITTED));
    }

    @Test
    @DisplayName("提交任务：未领取抛 9005")
    void submitWithoutAcceptanceThrows() {
        when(taskMapper.selectById(TASK_ID)).thenReturn(publishedTask());
        when(taskAcceptanceMapper.selectOne(any(), anyBoolean())).thenReturn(null);

        assertThatThrownBy(() -> taskService.submit(
                        TASK_ID, TaskSubmitRequest.builder().progress(80).build()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(TaskErrorConstants.TASK_ACCEPTANCE_NOT_FOUND));
    }

    @Test
    @DisplayName("提交任务：进度超出范围抛 9008")
    void submitInvalidProgressThrows() {
        when(taskMapper.selectById(TASK_ID)).thenReturn(publishedTask());

        assertThatThrownBy(() -> taskService.submit(
                        TASK_ID, TaskSubmitRequest.builder().progress(101).build()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(TaskErrorConstants.TASK_PROGRESS_INVALID));
        verify(taskAcceptanceMapper, never()).updateById(any(TaskAcceptance.class));
    }

    @Test
    @DisplayName("提交任务：已关闭任务抛 9003")
    void submitClosedTaskThrows() {
        when(taskMapper.selectById(TASK_ID))
                .thenReturn(Task.builder().id(TASK_ID).status(TaskStatus.CLOSED).build());

        assertThatThrownBy(() -> taskService.submit(
                        TASK_ID, TaskSubmitRequest.builder().progress(100).build()))
                .isInstanceOf(BusinessException.class)
                .satisfies(
                        e -> assertThat(((BusinessException) e).getCode()).isEqualTo(TaskErrorConstants.TASK_CLOSED));
    }

    @Test
    @DisplayName("提交任务：任务不存在抛 9001")
    void submitMissingTaskThrows() {
        when(taskMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> taskService.submit(
                        999L, TaskSubmitRequest.builder().progress(100).build()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e ->
                        assertThat(((BusinessException) e).getCode()).isEqualTo(TaskErrorConstants.TASK_NOT_FOUND));
    }

    @Test
    @DisplayName("提交任务：进度为 null 抛 9008")
    void submitNullProgressThrows() {
        when(taskMapper.selectById(TASK_ID)).thenReturn(publishedTask());

        assertThatThrownBy(() -> taskService.submit(
                        TASK_ID, TaskSubmitRequest.builder().progress(null).build()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(TaskErrorConstants.TASK_PROGRESS_INVALID));
    }

    @Test
    @DisplayName("提交任务：进度为负数抛 9008")
    void submitNegativeProgressThrows() {
        when(taskMapper.selectById(TASK_ID)).thenReturn(publishedTask());

        assertThatThrownBy(() -> taskService.submit(
                        TASK_ID, TaskSubmitRequest.builder().progress(-1).build()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(TaskErrorConstants.TASK_PROGRESS_INVALID));
    }

    @Test
    @DisplayName("领取任务：落库失败抛 9007")
    void acceptSaveFailsThrows() {
        when(taskMapper.selectById(TASK_ID)).thenReturn(publishedTask());
        when(taskAcceptanceMapper.selectCount(any())).thenReturn(0L);
        when(taskAcceptanceMapper.insert(any(TaskAcceptance.class))).thenReturn(0);

        assertThatThrownBy(() -> taskService.accept(TASK_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(TaskErrorConstants.TASK_SAVE_FAILED));
    }

    @Test
    @DisplayName("提交任务：更新失败抛 9009")
    void submitUpdateFailsThrows() {
        when(taskMapper.selectById(TASK_ID)).thenReturn(publishedTask());
        when(taskAcceptanceMapper.selectOne(any(), anyBoolean()))
                .thenReturn(acceptance(TaskAcceptanceStatus.ACCEPTED, 0));
        when(taskAcceptanceMapper.updateById(any(TaskAcceptance.class))).thenReturn(0);

        assertThatThrownBy(() -> taskService.submit(
                        TASK_ID, TaskSubmitRequest.builder().progress(50).build()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(TaskErrorConstants.TASK_UPDATE_FAILED));
    }

    @Test
    @DisplayName("领取任务：未登录抛 RESOURCE_NOT_AUTHORIZED")
    void acceptNotLoggedInThrows() {
        when(taskMapper.selectById(TASK_ID)).thenReturn(publishedTask());
        mockLoggedOut();

        assertThatThrownBy(() -> taskService.accept(TASK_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED));
    }

    // ==================== helpers ====================

    private Task publishedTask() {
        return Task.builder().id(TASK_ID).status(TaskStatus.PUBLISHED).build();
    }

    private TaskAcceptance acceptance(TaskAcceptanceStatus status, int progress) {
        return TaskAcceptance.builder()
                .id(10L)
                .taskId(TASK_ID)
                .userId(STUDENT_ID)
                .progress(progress)
                .status(status)
                .build();
    }

    private void mockLoggedIn(Long userId) {
        closeMock();
        stpUtilMock = mockStatic(StpUtil.class);
        stpUtilMock.when(StpUtil::isLogin).thenReturn(true);
        stpUtilMock.when(StpUtil::getLoginIdAsString).thenReturn(String.valueOf(userId));
    }

    private void mockLoggedOut() {
        closeMock();
        stpUtilMock = mockStatic(StpUtil.class);
        stpUtilMock.when(StpUtil::isLogin).thenReturn(false);
    }

    private void closeMock() {
        if (stpUtilMock != null) {
            stpUtilMock.close();
            stpUtilMock = null;
        }
    }
}
