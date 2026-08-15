package com.rauio.smartdangjian.server.task.service;

import static com.rauio.smartdangjian.constants.ErrorConstants.RESOURCE_NOT_AUTHORIZED;
import static com.rauio.smartdangjian.server.task.constants.TaskErrorConstants.TASK_ACCEPTANCE_NOT_FOUND;
import static com.rauio.smartdangjian.server.task.constants.TaskErrorConstants.TASK_ALREADY_ACCEPTED;
import static com.rauio.smartdangjian.server.task.constants.TaskErrorConstants.TASK_ALREADY_SUBMITTED;
import static com.rauio.smartdangjian.server.task.constants.TaskErrorConstants.TASK_CLOSED;
import static com.rauio.smartdangjian.server.task.constants.TaskErrorConstants.TASK_NOT_FOUND;
import static com.rauio.smartdangjian.server.task.constants.TaskErrorConstants.TASK_NOT_PUBLISHED;
import static com.rauio.smartdangjian.server.task.constants.TaskErrorConstants.TASK_PROGRESS_INVALID;
import static com.rauio.smartdangjian.server.task.constants.TaskErrorConstants.TASK_SAVE_FAILED;
import static com.rauio.smartdangjian.server.task.constants.TaskErrorConstants.TASK_UPDATE_FAILED;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.task.mapper.TaskAcceptanceMapper;
import com.rauio.smartdangjian.server.task.mapper.TaskMapper;
import com.rauio.smartdangjian.server.task.pojo.convertor.TaskConvertor;
import com.rauio.smartdangjian.server.task.pojo.entity.Task;
import com.rauio.smartdangjian.server.task.pojo.entity.TaskAcceptance;
import com.rauio.smartdangjian.server.task.pojo.request.TaskSubmitRequest;
import com.rauio.smartdangjian.server.task.pojo.response.TaskAcceptanceResponse;
import com.rauio.smartdangjian.server.task.pojo.response.TaskPageResponse;
import com.rauio.smartdangjian.server.task.spec.TaskAcceptanceStatus;
import com.rauio.smartdangjian.server.task.spec.TaskStatus;
import com.rauio.smartdangjian.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

/**
 * 任务服务（用户端）：分页查看已发布任务、领取任务、提交任务（含进度更新）。
 */
@Service
@RequiredArgsConstructor
public class TaskService extends ServiceImpl<TaskAcceptanceMapper, TaskAcceptance> {

    private final TaskMapper taskMapper;
    private final TaskConvertor taskConvertor;

    /**
     * 分页查询已发布（published）任务，按创建时间倒序。
     *
     * @param pageNum  页码（从1开始）
     * @param pageSize 每页大小
     * @return 任务分页响应
     */
    public TaskPageResponse getPublishedPage(int pageNum, int pageSize) {
        Page<Task> page = new Page<>(pageNum, pageSize);
        taskMapper.selectPage(
                page,
                new LambdaQueryWrapper<Task>()
                        .eq(Task::getStatus, TaskStatus.PUBLISHED)
                        .orderByDesc(Task::getCreatedAt));
        return TaskPageResponse.builder()
                .total(page.getTotal())
                .size(page.getSize())
                .current(page.getCurrent())
                .records(taskConvertor.toResponseList(page.getRecords()))
                .build();
    }

    /**
     * 领取任务：仅已发布任务可领取，同一用户同一任务不可重复领取。
     *
     * @param taskId 任务ID
     * @return 领取记录响应
     */
    public TaskAcceptanceResponse accept(Long taskId) {
        requirePublishedTask(taskId);
        Long userId = currentUserId();
        if (countAcceptance(taskId, userId) > 0) {
            throw new BusinessException(TASK_ALREADY_ACCEPTED, "任务已领取，请勿重复领取");
        }
        TaskAcceptance acceptance = TaskAcceptance.builder()
                .taskId(taskId)
                .userId(userId)
                .progress(0)
                .status(TaskAcceptanceStatus.ACCEPTED)
                .acceptedAt(LocalDateTime.now())
                .build();
        try {
            if (!this.save(acceptance)) {
                throw new BusinessException(TASK_SAVE_FAILED, "任务领取失败");
            }
        } catch (org.springframework.dao.DuplicateKeyException e) {
            // 并发领取：唯一约束 uk_task_acceptance_task_user 冲突视为重复领取
            throw new BusinessException(TASK_ALREADY_ACCEPTED, "任务已领取，请勿重复领取");
        }
        return taskConvertor.toAcceptanceResponse(acceptance);
    }

    /**
     * 提交任务（含进度更新）：仅已领取任务可提交，提交进度达到100视为完成；
     * 已提交（submitted）/已完成（completed）/已驳回（rejected）的记录不可重复提交。
     *
     * @param taskId  任务ID
     * @param request 提交任务请求
     * @return 领取记录响应
     */
    public TaskAcceptanceResponse submit(Long taskId, TaskSubmitRequest request) {
        requirePublishedTask(taskId);
        Integer progress = request.getProgress();
        if (progress == null || progress < 0 || progress > 100) {
            throw new BusinessException(TASK_PROGRESS_INVALID, "完成进度必须介于0-100");
        }
        Long userId = currentUserId();
        TaskAcceptance acceptance = getAcceptance(taskId, userId);
        if (acceptance.getStatus() == TaskAcceptanceStatus.SUBMITTED
                || acceptance.getStatus() == TaskAcceptanceStatus.COMPLETED
                || acceptance.getStatus() == TaskAcceptanceStatus.REJECTED) {
            throw new BusinessException(TASK_ALREADY_SUBMITTED, "任务已提交，请勿重复提交");
        }
        acceptance.setProgress(progress);
        if (progress >= 100) {
            acceptance.setStatus(TaskAcceptanceStatus.COMPLETED);
            acceptance.setCompletedAt(LocalDateTime.now());
        } else {
            acceptance.setStatus(TaskAcceptanceStatus.SUBMITTED);
        }
        if (!this.updateById(acceptance)) {
            throw new BusinessException(TASK_UPDATE_FAILED, "任务提交失败");
        }
        return taskConvertor.toAcceptanceResponse(acceptance);
    }

    /**
     * 校验任务存在且已发布；已关闭任务抛 TASK_CLOSED，草稿任务抛 TASK_NOT_PUBLISHED。
     *
     * @param taskId 任务ID
     * @return 任务实体
     */
    private Task requirePublishedTask(Long taskId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(TASK_NOT_FOUND, "任务不存在");
        }
        if (task.getStatus() == TaskStatus.CLOSED) {
            throw new BusinessException(TASK_CLOSED, "任务已关闭，无法操作");
        }
        if (task.getStatus() != TaskStatus.PUBLISHED) {
            throw new BusinessException(TASK_NOT_PUBLISHED, "任务未发布，无法操作");
        }
        return task;
    }

    /**
     * 统计指定用户对指定任务的领取记录数。
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 记录数
     */
    private long countAcceptance(Long taskId, Long userId) {
        return this.count(new LambdaQueryWrapper<TaskAcceptance>()
                .eq(TaskAcceptance::getTaskId, taskId)
                .eq(TaskAcceptance::getUserId, userId));
    }

    /**
     * 查询指定用户对指定任务的领取记录，不存在时抛 TASK_ACCEPTANCE_NOT_FOUND。
     *
     * @param taskId 任务ID
     * @param userId 用户ID
     * @return 领取记录
     */
    private TaskAcceptance getAcceptance(Long taskId, Long userId) {
        TaskAcceptance acceptance = this.getOne(new LambdaQueryWrapper<TaskAcceptance>()
                .eq(TaskAcceptance::getTaskId, taskId)
                .eq(TaskAcceptance::getUserId, userId));
        if (acceptance == null) {
            throw new BusinessException(TASK_ACCEPTANCE_NOT_FOUND, "未领取任务，无法提交");
        }
        return acceptance;
    }

    /**
     * 获取当前登录用户 ID。
     *
     * @return 用户ID
     */
    private Long currentUserId() {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null || userId.isBlank()) {
            throw new BusinessException(RESOURCE_NOT_AUTHORIZED, "请先登录");
        }
        return Long.valueOf(userId);
    }
}
