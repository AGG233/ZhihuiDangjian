package com.rauio.smartdangjian.server.task.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

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
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.task.constants.TaskErrorConstants;
import com.rauio.smartdangjian.server.task.mapper.TaskAcceptanceMapper;
import com.rauio.smartdangjian.server.task.mapper.TaskMapper;
import com.rauio.smartdangjian.server.task.pojo.convertor.TaskConvertor;
import com.rauio.smartdangjian.server.task.pojo.entity.Task;
import com.rauio.smartdangjian.server.task.pojo.entity.TaskAcceptance;
import com.rauio.smartdangjian.server.task.pojo.request.TaskCreateRequest;
import com.rauio.smartdangjian.server.task.pojo.request.TaskUpdateRequest;
import com.rauio.smartdangjian.server.task.pojo.response.TaskResponse;
import com.rauio.smartdangjian.server.task.spec.TaskStatus;
import com.rauio.smartdangjian.server.task.spec.TaskType;

import cn.dev33.satoken.stp.StpUtil;

/**
 * 管理端任务服务单元测试：创建、更新（含状态流转校验）、删除。
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdminTaskServiceTest {

    private static final Long ADMIN_ID = 10L;

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
    private TaskAcceptanceMapper taskAcceptanceMapper;

    @Mock
    private TaskConvertor taskConvertor;

    @Spy
    @InjectMocks
    private AdminTaskService adminTaskService;

    private MockedStatic<StpUtil> stpUtilMock;

    @BeforeEach
    void setUp() {
        lenient().doReturn(taskMapper).when(adminTaskService).getBaseMapper();
        lenient().when(taskMapper.insert(any(Task.class))).thenReturn(1);
        lenient()
                .when(taskConvertor.toResponse(any(Task.class)))
                .thenReturn(TaskResponse.builder().id(1L).build());
        mockLoggedIn(ADMIN_ID);
    }

    @AfterEach
    void tearDown() {
        closeMock();
    }

    // ==================== create ====================

    @Test
    @DisplayName("创建任务：初始状态为草稿，创建者为当前登录管理员")
    void createPersistsDraftWithCurrentCreator() {
        TaskCreateRequest request = TaskCreateRequest.builder()
                .title("参加一次志愿活动")
                .description("完成社区志愿服务并提交心得")
                .taskType(TaskType.SOCIAL)
                .points(20)
                .deadline(LocalDateTime.of(2026, 9, 1, 0, 0))
                .build();

        TaskResponse response = adminTaskService.create(request);

        assertThat(response).isNotNull();
        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskMapper).insert(captor.capture());
        Task saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("参加一次志愿活动");
        assertThat(saved.getDescription()).isEqualTo("完成社区志愿服务并提交心得");
        assertThat(saved.getTaskType()).isEqualTo(TaskType.SOCIAL);
        assertThat(saved.getPoints()).isEqualTo(20);
        assertThat(saved.getDeadline()).isEqualTo(LocalDateTime.of(2026, 9, 1, 0, 0));
        assertThat(saved.getCreatorId()).isEqualTo(ADMIN_ID);
        assertThat(saved.getStatus()).isEqualTo(TaskStatus.DRAFT);
    }

    @Test
    @DisplayName("创建任务：points 为空时默认为 0")
    void createDefaultsPointsToZero() {
        adminTaskService.create(TaskCreateRequest.builder()
                .title("任务")
                .taskType(TaskType.CUSTOM)
                .build());

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskMapper).insert(captor.capture());
        assertThat(captor.getValue().getPoints()).isZero();
    }

    @Test
    @DisplayName("创建任务：未登录时抛 RESOURCE_NOT_AUTHORIZED")
    void createWithoutLoginThrows() {
        mockLoggedOut();

        assertThatThrownBy(() -> adminTaskService.create(TaskCreateRequest.builder()
                        .title("任务")
                        .taskType(TaskType.CUSTOM)
                        .build()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(ErrorConstants.RESOURCE_NOT_AUTHORIZED));
        verify(taskMapper, never()).insert(any(Task.class));
    }

    // ==================== update ====================

    @Test
    @DisplayName("更新任务：仅更新字段不改变状态")
    void updateChangesFieldsOnly() {
        when(taskMapper.selectById(1L))
                .thenReturn(Task.builder().id(1L).status(TaskStatus.DRAFT).build());
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        adminTaskService.update(
                1L, TaskUpdateRequest.builder().title("新标题").points(30).build());

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskMapper).updateById(captor.capture());
        Task updated = captor.getValue();
        assertThat(updated.getTitle()).isEqualTo("新标题");
        assertThat(updated.getPoints()).isEqualTo(30);
        assertThat(updated.getStatus()).isEqualTo(TaskStatus.DRAFT);
    }

    @Test
    @DisplayName("状态流转：draft → published 合法")
    void transitionDraftToPublishedIsValid() {
        when(taskMapper.selectById(1L))
                .thenReturn(Task.builder().id(1L).status(TaskStatus.DRAFT).build());
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        adminTaskService.update(
                1L, TaskUpdateRequest.builder().status(TaskStatus.PUBLISHED).build());

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TaskStatus.PUBLISHED);
    }

    @Test
    @DisplayName("状态流转：draft → closed 合法")
    void transitionDraftToClosedIsValid() {
        when(taskMapper.selectById(1L))
                .thenReturn(Task.builder().id(1L).status(TaskStatus.DRAFT).build());
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        adminTaskService.update(
                1L, TaskUpdateRequest.builder().status(TaskStatus.CLOSED).build());

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TaskStatus.CLOSED);
    }

    @Test
    @DisplayName("状态流转：published → closed 合法")
    void transitionPublishedToClosedIsValid() {
        when(taskMapper.selectById(1L))
                .thenReturn(Task.builder().id(1L).status(TaskStatus.PUBLISHED).build());
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        adminTaskService.update(
                1L, TaskUpdateRequest.builder().status(TaskStatus.CLOSED).build());

        verify(taskMapper).updateById(any(Task.class));
    }

    @Test
    @DisplayName("状态流转：published → draft 非法抛 9007")
    void transitionPublishedToDraftIsRejected() {
        when(taskMapper.selectById(1L))
                .thenReturn(Task.builder().id(1L).status(TaskStatus.PUBLISHED).build());

        assertThatThrownBy(() -> adminTaskService.update(
                        1L, TaskUpdateRequest.builder().status(TaskStatus.DRAFT).build()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(TaskErrorConstants.TASK_INVALID_STATUS_TRANSITION));
        verify(taskMapper, never()).updateById(any(Task.class));
    }

    @Test
    @DisplayName("状态流转：closed → published 非法抛 9007")
    void transitionClosedToPublishedIsRejected() {
        when(taskMapper.selectById(1L))
                .thenReturn(Task.builder().id(1L).status(TaskStatus.CLOSED).build());

        assertThatThrownBy(() -> adminTaskService.update(
                        1L,
                        TaskUpdateRequest.builder().status(TaskStatus.PUBLISHED).build()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(TaskErrorConstants.TASK_INVALID_STATUS_TRANSITION));
        verify(taskMapper, never()).updateById(any(Task.class));
    }

    @Test
    @DisplayName("更新任务：任务不存在抛 9001")
    void updateNotFoundTaskThrows() {
        when(taskMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> adminTaskService.update(
                        999L, TaskUpdateRequest.builder().build()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e ->
                        assertThat(((BusinessException) e).getCode()).isEqualTo(TaskErrorConstants.TASK_NOT_FOUND));
    }

    // ==================== delete ====================

    @Test
    @DisplayName("删除任务：级联清理领取记录后删除任务")
    void deleteRemovesAcceptancesAndTask() {
        when(taskMapper.selectById(1L)).thenReturn(Task.builder().id(1L).build());
        when(taskAcceptanceMapper.delete(any())).thenReturn(1);
        when(taskMapper.deleteById(1L)).thenReturn(1);

        adminTaskService.delete(1L);

        verify(taskAcceptanceMapper).delete(any());
        verify(taskMapper).deleteById(1L);
    }

    @Test
    @DisplayName("删除任务：任务不存在抛 9001")
    void deleteNotFoundTaskThrows() {
        when(taskMapper.selectById(999L)).thenReturn(null);

        assertThatThrownBy(() -> adminTaskService.delete(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e ->
                        assertThat(((BusinessException) e).getCode()).isEqualTo(TaskErrorConstants.TASK_NOT_FOUND));
        verify(taskMapper, never()).deleteById(any(Long.class));
    }

    @Test
    @DisplayName("创建任务：落库失败抛 9005")
    void createSaveFailsThrows() {
        when(taskMapper.insert(any(Task.class))).thenReturn(0);

        assertThatThrownBy(() -> adminTaskService.create(TaskCreateRequest.builder()
                        .title("任务")
                        .taskType(TaskType.CUSTOM)
                        .build()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(TaskErrorConstants.TASK_SAVE_FAILED));
    }

    @Test
    @DisplayName("更新任务：更新失败抛 9006")
    void updateUpdateFailsThrows() {
        when(taskMapper.selectById(1L))
                .thenReturn(Task.builder().id(1L).status(TaskStatus.DRAFT).build());
        when(taskMapper.updateById(any(Task.class))).thenReturn(0);

        assertThatThrownBy(() -> adminTaskService.update(
                        1L, TaskUpdateRequest.builder().title("新标题").build()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(TaskErrorConstants.TASK_UPDATE_FAILED));
    }

    @Test
    @DisplayName("删除任务：删除失败抛 9008")
    void deleteRemoveFailsThrows() {
        when(taskMapper.selectById(1L)).thenReturn(Task.builder().id(1L).build());
        when(taskMapper.deleteById(1L)).thenReturn(0);

        assertThatThrownBy(() -> adminTaskService.delete(1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(TaskErrorConstants.TASK_DELETE_FAILED));
    }

    @Test
    @DisplayName("更新任务：全字段更新均落库")
    void updateAllFieldsPersists() {
        when(taskMapper.selectById(1L))
                .thenReturn(Task.builder().id(1L).status(TaskStatus.DRAFT).build());
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        adminTaskService.update(
                1L,
                TaskUpdateRequest.builder()
                        .title("新标题")
                        .description("新描述")
                        .taskType(TaskType.LEARNING)
                        .points(50)
                        .deadline(LocalDateTime.of(2026, 10, 1, 0, 0))
                        .build());

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskMapper).updateById(captor.capture());
        Task updated = captor.getValue();
        assertThat(updated.getTitle()).isEqualTo("新标题");
        assertThat(updated.getDescription()).isEqualTo("新描述");
        assertThat(updated.getTaskType()).isEqualTo(TaskType.LEARNING);
        assertThat(updated.getPoints()).isEqualTo(50);
        assertThat(updated.getDeadline()).isEqualTo(LocalDateTime.of(2026, 10, 1, 0, 0));
    }

    @Test
    @DisplayName("状态流转：draft → draft 非法抛 9007")
    void transitionDraftToDraftIsRejected() {
        when(taskMapper.selectById(1L))
                .thenReturn(Task.builder().id(1L).status(TaskStatus.DRAFT).build());

        assertThatThrownBy(() -> adminTaskService.update(
                        1L, TaskUpdateRequest.builder().status(TaskStatus.DRAFT).build()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(TaskErrorConstants.TASK_INVALID_STATUS_TRANSITION));
        verify(taskMapper, never()).updateById(any(Task.class));
    }

    // ==================== helpers ====================

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
