package com.rauio.smartdangjian.crosslayer.task;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.task.constants.TaskErrorConstants;
import com.rauio.smartdangjian.server.task.mapper.TaskAcceptanceMapper;
import com.rauio.smartdangjian.server.task.mapper.TaskMapper;
import com.rauio.smartdangjian.server.task.pojo.convertor.TaskConvertor;
import com.rauio.smartdangjian.server.task.pojo.entity.Task;
import com.rauio.smartdangjian.server.task.pojo.entity.TaskAcceptance;
import com.rauio.smartdangjian.server.task.pojo.request.TaskCreateRequest;
import com.rauio.smartdangjian.server.task.pojo.request.TaskSubmitRequest;
import com.rauio.smartdangjian.server.task.pojo.request.TaskUpdateRequest;
import com.rauio.smartdangjian.server.task.pojo.response.TaskAcceptanceResponse;
import com.rauio.smartdangjian.server.task.pojo.response.TaskPageResponse;
import com.rauio.smartdangjian.server.task.pojo.response.TaskResponse;
import com.rauio.smartdangjian.server.task.service.AdminTaskService;
import com.rauio.smartdangjian.server.task.service.TaskService;
import com.rauio.smartdangjian.server.task.spec.TaskAcceptanceStatus;
import com.rauio.smartdangjian.server.task.spec.TaskStatus;
import com.rauio.smartdangjian.server.task.spec.TaskType;

/**
 * 任务发布与领取跨层回归测试。
 *
 * <p>装配真实 AdminTaskService 与 TaskService，TaskMapper/TaskAcceptanceMapper/
 * TaskConvertor 以 {@link MockitoBean} 提供（Spring 在用例之间自动重置，
 * 沿用既有 CrossLayerTestBase 约定：H2 URL + Flyway 禁用 + 真实 Service）。
 * 通过捕获真实 insert()/updateById() 写入 Mapper 的实体，断言发布状态流转、
 * 领取防重复、提交状态机与非法流转拒绝行为。
 */
@SpringBootTest(classes = TaskAcceptanceCrossLayerTest.TestConfig.class)
class TaskAcceptanceCrossLayerTest extends CrossLayerTestBase {

    private static final Long TASK_ID = 1L;
    private static final Long SCHOOL_USER_ID = 10L;
    private static final Long STUDENT_ID = 100L;

    @BeforeAll
    static void initMybatisPlus() {
        MybatisConfiguration config = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(config, "");
        TableInfoHelper.initTableInfo(assistant, Task.class);
        TableInfoHelper.initTableInfo(assistant, TaskAcceptance.class);
    }

    @MockitoBean
    private TaskMapper taskMapper;

    @MockitoBean
    private TaskAcceptanceMapper taskAcceptanceMapper;

    @MockitoBean
    private TaskConvertor taskConvertor;

    @Autowired
    private AdminTaskService adminTaskService;

    @Autowired
    private TaskService taskService;

    @BeforeEach
    void stubSuccessPaths() {
        when(taskMapper.insert(any(Task.class))).thenReturn(1);
        when(taskAcceptanceMapper.insert(any(TaskAcceptance.class))).thenReturn(1);
        when(taskAcceptanceMapper.updateById(any(TaskAcceptance.class))).thenReturn(1);
        when(taskAcceptanceMapper.delete(any())).thenReturn(1);
        when(taskMapper.deleteById(TASK_ID)).thenReturn(1);
        when(taskConvertor.toResponse(any(Task.class)))
                .thenReturn(TaskResponse.builder().id(TASK_ID).build());
        when(taskConvertor.toResponseList(any()))
                .thenReturn(List.of(TaskResponse.builder().id(TASK_ID).build()));
        when(taskConvertor.toAcceptanceResponse(any(TaskAcceptance.class)))
                .thenReturn(TaskAcceptanceResponse.builder()
                        .taskId(TASK_ID)
                        .userId(STUDENT_ID)
                        .build());
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        AdminTaskService adminTaskService(
                TaskAcceptanceMapper acceptanceMapper, TaskConvertor convertor, TaskMapper taskMapper) {
            AdminTaskService service = new AdminTaskService(acceptanceMapper, convertor);
            injectBaseMapper(service, taskMapper);
            return service;
        }

        @Bean
        TaskService taskService(TaskMapper taskMapper, TaskConvertor convertor, TaskAcceptanceMapper acceptanceMapper) {
            TaskService service = new TaskService(taskMapper, convertor);
            injectBaseMapper(service, acceptanceMapper);
            return service;
        }

        @Bean
        AbstractPlatformTransactionManager transactionManager() {
            return new AbstractPlatformTransactionManager() {
                @Override
                protected Object doGetTransaction() {
                    return new Object();
                }

                @Override
                protected void doBegin(Object transaction, TransactionDefinition definition) {}

                @Override
                protected void doCommit(DefaultTransactionStatus status) {}

                @Override
                protected void doRollback(DefaultTransactionStatus status) {}
            };
        }

        private static <T, M> void injectBaseMapper(T service, M mapper) {
            try {
                Field field = findBaseMapperField(service.getClass());
                field.setAccessible(true);
                field.set(service, mapper);
            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to set baseMapper on " + service.getClass().getSimpleName(), e);
            }
        }

        @SuppressWarnings("PMD.AvoidAccessibilityAlteration")
        private static Field findBaseMapperField(Class<?> clazz) throws NoSuchFieldException {
            Class<?> current = clazz;
            while (current != null) {
                try {
                    return current.getDeclaredField("baseMapper");
                } catch (NoSuchFieldException e) {
                    current = current.getSuperclass();
                }
            }
            throw new NoSuchFieldException("baseMapper");
        }
    }

    // ==================== 管理端：发布管理 ====================

    @Test
    @DisplayName("管理端创建任务：落库状态为 draft，创建者为当前登录管理员")
    void adminCreateTaskPersistsDraft() {
        setSchoolContext(SCHOOL_USER_ID, "uni1");

        adminTaskService.create(TaskCreateRequest.builder()
                .title("参加一次志愿活动")
                .description("完成社区志愿服务并提交心得")
                .taskType(TaskType.SOCIAL)
                .points(20)
                .build());

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskMapper).insert(captor.capture());
        Task saved = captor.getValue();
        assertThat(saved.getStatus()).isEqualTo(TaskStatus.DRAFT);
        assertThat(saved.getCreatorId()).isEqualTo(SCHOOL_USER_ID);
        assertThat(saved.getTitle()).isEqualTo("参加一次志愿活动");
        assertThat(saved.getTaskType()).isEqualTo(TaskType.SOCIAL);
        assertThat(saved.getPoints()).isEqualTo(20);
    }

    @Test
    @DisplayName("管理端发布任务：draft → published 流转成功")
    void adminPublishTaskTransitionsToPublished() {
        setSchoolContext(SCHOOL_USER_ID, "uni1");
        when(taskMapper.selectById(TASK_ID))
                .thenReturn(Task.builder().id(TASK_ID).status(TaskStatus.DRAFT).build());
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);

        adminTaskService.update(
                TASK_ID,
                TaskUpdateRequest.builder().status(TaskStatus.PUBLISHED).build());

        ArgumentCaptor<Task> captor = ArgumentCaptor.forClass(Task.class);
        verify(taskMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(TaskStatus.PUBLISHED);
    }

    @Test
    @DisplayName("管理端状态流转：published → draft 非法抛 9007")
    void adminInvalidTransitionRejected() {
        setSchoolContext(SCHOOL_USER_ID, "uni1");
        when(taskMapper.selectById(TASK_ID))
                .thenReturn(
                        Task.builder().id(TASK_ID).status(TaskStatus.PUBLISHED).build());

        assertThatThrownBy(() -> adminTaskService.update(
                        TASK_ID,
                        TaskUpdateRequest.builder().status(TaskStatus.DRAFT).build()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(TaskErrorConstants.TASK_INVALID_STATUS_TRANSITION));
        verify(taskMapper, never()).updateById(any(Task.class));
    }

    @Test
    @DisplayName("管理端删除任务：级联清理领取记录后删除任务")
    void adminDeleteTaskRemovesAcceptancesAndTask() {
        setSchoolContext(SCHOOL_USER_ID, "uni1");
        when(taskMapper.selectById(TASK_ID))
                .thenReturn(Task.builder().id(TASK_ID).build());

        adminTaskService.delete(TASK_ID);

        verify(taskAcceptanceMapper).delete(any());
        verify(taskMapper).deleteById(TASK_ID);
    }

    // ==================== 用户端：分页 ====================

    @Test
    @DisplayName("用户分页查询：仅查询已发布任务")
    void userGetPublishedPageFiltersPublishedOnly() {
        setStudentContext(STUDENT_ID, "uni1");
        Page<Task> page = new Page<>(1, 10, 1);
        doAnswer(invocation -> {
                    Page<Task> arg = invocation.getArgument(0);
                    arg.setRecords(List.of(Task.builder().id(TASK_ID).build()));
                    arg.setTotal(3);
                    return null;
                })
                .when(taskMapper)
                .selectPage(any(Page.class), any());

        TaskPageResponse result = taskService.getPublishedPage(1, 10);

        ArgumentCaptor<LambdaQueryWrapper<Task>> wrapperCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(taskMapper).selectPage(any(Page.class), wrapperCaptor.capture());
        LambdaQueryWrapper<Task> query = wrapperCaptor.getValue();
        // formatParam 惰性执行：getSqlSegment() 触发参数填充后再断言过滤条件
        query.getSqlSegment();
        Map<String, Object> params = query.getParamNameValuePairs();
        assertThat(params.values()).contains(TaskStatus.PUBLISHED);
        assertThat(result.getTotal()).isEqualTo(3);
        assertThat(result.getRecords()).hasSize(1);
    }

    // ==================== 用户端：领取 ====================

    @Test
    @DisplayName("学生领取已发布任务：落库 accepted 记录，进度为 0")
    void studentAcceptTaskPersistsAcceptedRecord() {
        setStudentContext(STUDENT_ID, "uni1");
        when(taskMapper.selectById(TASK_ID))
                .thenReturn(
                        Task.builder().id(TASK_ID).status(TaskStatus.PUBLISHED).build());
        when(taskAcceptanceMapper.selectCount(any())).thenReturn(0L);

        taskService.accept(TASK_ID);

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
    @DisplayName("学生重复领取任务：抛 9002 且不落库")
    void studentDuplicateAcceptThrows() {
        setStudentContext(STUDENT_ID, "uni1");
        when(taskMapper.selectById(TASK_ID))
                .thenReturn(
                        Task.builder().id(TASK_ID).status(TaskStatus.PUBLISHED).build());
        when(taskAcceptanceMapper.selectCount(any())).thenReturn(1L);

        assertThatThrownBy(() -> taskService.accept(TASK_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(TaskErrorConstants.TASK_ALREADY_ACCEPTED));
        verify(taskAcceptanceMapper, never()).insert(any(TaskAcceptance.class));
    }

    @Test
    @DisplayName("学生领取已关闭任务：抛 9003 且不落库")
    void studentAcceptClosedTaskThrows() {
        setStudentContext(STUDENT_ID, "uni1");
        when(taskMapper.selectById(TASK_ID))
                .thenReturn(Task.builder().id(TASK_ID).status(TaskStatus.CLOSED).build());

        assertThatThrownBy(() -> taskService.accept(TASK_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(
                        e -> assertThat(((BusinessException) e).getCode()).isEqualTo(TaskErrorConstants.TASK_CLOSED));
        verify(taskAcceptanceMapper, never()).insert(any(TaskAcceptance.class));
    }

    // ==================== 用户端：提交 ====================

    @Test
    @DisplayName("学生提交任务进度100：落库 completed 并记录完成时间")
    void studentSubmitFullProgressCompletes() {
        setStudentContext(STUDENT_ID, "uni1");
        when(taskMapper.selectById(TASK_ID))
                .thenReturn(
                        Task.builder().id(TASK_ID).status(TaskStatus.PUBLISHED).build());
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
    @DisplayName("学生提交任务进度50：落库 submitted 并保留进度")
    void studentSubmitPartialProgressMarksSubmitted() {
        setStudentContext(STUDENT_ID, "uni1");
        when(taskMapper.selectById(TASK_ID))
                .thenReturn(
                        Task.builder().id(TASK_ID).status(TaskStatus.PUBLISHED).build());
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
    @DisplayName("学生已提交后再提交：抛 9006 且不更新")
    void studentResubmitAfterSubmittedThrows() {
        setStudentContext(STUDENT_ID, "uni1");
        when(taskMapper.selectById(TASK_ID))
                .thenReturn(
                        Task.builder().id(TASK_ID).status(TaskStatus.PUBLISHED).build());
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
    @DisplayName("学生未领取任务直接提交：抛 9005")
    void studentSubmitWithoutAcceptanceThrows() {
        setStudentContext(STUDENT_ID, "uni1");
        when(taskMapper.selectById(TASK_ID))
                .thenReturn(
                        Task.builder().id(TASK_ID).status(TaskStatus.PUBLISHED).build());
        when(taskAcceptanceMapper.selectOne(any(), anyBoolean())).thenReturn(null);

        assertThatThrownBy(() -> taskService.submit(
                        TASK_ID, TaskSubmitRequest.builder().progress(80).build()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(TaskErrorConstants.TASK_ACCEPTANCE_NOT_FOUND));
        verify(taskAcceptanceMapper, never()).updateById(any(TaskAcceptance.class));
    }

    @Test
    @DisplayName("学生提交已关闭任务：抛 9003")
    void studentSubmitClosedTaskThrows() {
        setStudentContext(STUDENT_ID, "uni1");
        when(taskMapper.selectById(TASK_ID))
                .thenReturn(Task.builder().id(TASK_ID).status(TaskStatus.CLOSED).build());

        assertThatThrownBy(() -> taskService.submit(
                        TASK_ID, TaskSubmitRequest.builder().progress(100).build()))
                .isInstanceOf(BusinessException.class)
                .satisfies(
                        e -> assertThat(((BusinessException) e).getCode()).isEqualTo(TaskErrorConstants.TASK_CLOSED));
        verify(taskAcceptanceMapper, never()).updateById(any(TaskAcceptance.class));
    }

    @Test
    @DisplayName("学生提交进度超出范围：抛 9008")
    void studentSubmitInvalidProgressThrows() {
        setStudentContext(STUDENT_ID, "uni1");
        when(taskMapper.selectById(TASK_ID))
                .thenReturn(
                        Task.builder().id(TASK_ID).status(TaskStatus.PUBLISHED).build());

        assertThatThrownBy(() -> taskService.submit(
                        TASK_ID, TaskSubmitRequest.builder().progress(101).build()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).getCode())
                        .isEqualTo(TaskErrorConstants.TASK_PROGRESS_INVALID));
        verify(taskAcceptanceMapper, never()).updateById(any(TaskAcceptance.class));
    }

    // ==================== helpers ====================

    private TaskAcceptance acceptance(TaskAcceptanceStatus status, int progress) {
        return TaskAcceptance.builder()
                .id(10L)
                .taskId(TASK_ID)
                .userId(STUDENT_ID)
                .progress(progress)
                .status(status)
                .build();
    }
}
