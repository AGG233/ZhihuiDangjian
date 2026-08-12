package com.rauio.smartdangjian.server.task.service;

import static com.rauio.smartdangjian.constants.ErrorConstants.RESOURCE_NOT_AUTHORIZED;
import static com.rauio.smartdangjian.server.task.constants.TaskErrorConstants.TASK_DELETE_FAILED;
import static com.rauio.smartdangjian.server.task.constants.TaskErrorConstants.TASK_INVALID_STATUS_TRANSITION;
import static com.rauio.smartdangjian.server.task.constants.TaskErrorConstants.TASK_NOT_FOUND;
import static com.rauio.smartdangjian.server.task.constants.TaskErrorConstants.TASK_SAVE_FAILED;
import static com.rauio.smartdangjian.server.task.constants.TaskErrorConstants.TASK_UPDATE_FAILED;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.task.mapper.TaskAcceptanceMapper;
import com.rauio.smartdangjian.server.task.mapper.TaskMapper;
import com.rauio.smartdangjian.server.task.pojo.convertor.TaskConvertor;
import com.rauio.smartdangjian.server.task.pojo.entity.Task;
import com.rauio.smartdangjian.server.task.pojo.entity.TaskAcceptance;
import com.rauio.smartdangjian.server.task.pojo.request.TaskCreateRequest;
import com.rauio.smartdangjian.server.task.pojo.request.TaskUpdateRequest;
import com.rauio.smartdangjian.server.task.pojo.response.TaskResponse;
import com.rauio.smartdangjian.server.task.spec.TaskStatus;
import com.rauio.smartdangjian.utils.SecurityUtils;

import lombok.RequiredArgsConstructor;

/**
 * 任务管理服务（管理端）：创建、更新（含状态流转 draft→published→closed）、删除。
 */
@Service
@RequiredArgsConstructor
public class AdminTaskService extends ServiceImpl<TaskMapper, Task> {

    private final TaskAcceptanceMapper taskAcceptanceMapper;
    private final TaskConvertor taskConvertor;

    /**
     * 创建任务：初始状态固定为草稿（draft），创建者为当前登录管理员。
     *
     * @param request 创建任务请求
     * @return 任务响应
     */
    public TaskResponse create(TaskCreateRequest request) {
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .taskType(request.getTaskType())
                .points(request.getPoints() == null ? 0 : request.getPoints())
                .deadline(request.getDeadline())
                .creatorId(currentUserId())
                .status(TaskStatus.DRAFT)
                .build();
        if (!this.save(task)) {
            throw new BusinessException(TASK_SAVE_FAILED, "任务创建失败");
        }
        return taskConvertor.toResponse(task);
    }

    /**
     * 更新任务信息与状态。状态仅允许单向流转 draft→published→closed，
     * 非法流转抛 {@link TaskErrorConstants#TASK_INVALID_STATUS_TRANSITION}。
     *
     * @param id      任务ID
     * @param request 更新任务请求
     * @return 任务响应
     */
    public TaskResponse update(Long id, TaskUpdateRequest request) {
        Task task = this.getById(id);
        if (task == null) {
            throw new BusinessException(TASK_NOT_FOUND, "任务不存在");
        }
        if (request.getStatus() != null) {
            assertValidTransition(task.getStatus(), request.getStatus());
            task.setStatus(request.getStatus());
        }
        if (request.getTitle() != null) {
            task.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getTaskType() != null) {
            task.setTaskType(request.getTaskType());
        }
        if (request.getPoints() != null) {
            task.setPoints(request.getPoints());
        }
        if (request.getDeadline() != null) {
            task.setDeadline(request.getDeadline());
        }
        if (!this.updateById(task)) {
            throw new BusinessException(TASK_UPDATE_FAILED, "任务更新失败");
        }
        return taskConvertor.toResponse(task);
    }

    /**
     * 删除任务：级联清理该任务的全部领取记录。
     *
     * @param id 任务ID
     */
    public void delete(Long id) {
        if (this.getById(id) == null) {
            throw new BusinessException(TASK_NOT_FOUND, "任务不存在");
        }
        taskAcceptanceMapper.delete(new LambdaQueryWrapper<TaskAcceptance>().eq(TaskAcceptance::getTaskId, id));
        if (!this.removeById(id)) {
            throw new BusinessException(TASK_DELETE_FAILED, "任务删除失败");
        }
    }

    /**
     * 校验任务状态单向流转是否合法（draft→published、draft→closed、published→closed）。
     *
     * @param from 当前状态
     * @param to   目标状态
     */
    private void assertValidTransition(TaskStatus from, TaskStatus to) {
        boolean valid =
                switch (from) {
                    case DRAFT -> to == TaskStatus.PUBLISHED || to == TaskStatus.CLOSED;
                    case PUBLISHED -> to == TaskStatus.CLOSED;
                    case CLOSED -> false;
                };
        if (!valid) {
            throw new BusinessException(TASK_INVALID_STATUS_TRANSITION, "非法任务状态流转: " + from + " → " + to);
        }
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
