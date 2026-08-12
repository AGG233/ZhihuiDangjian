package com.rauio.smartdangjian.server.task.pojo.convertor;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.rauio.smartdangjian.server.task.pojo.entity.Task;
import com.rauio.smartdangjian.server.task.pojo.entity.TaskAcceptance;
import com.rauio.smartdangjian.server.task.pojo.response.TaskAcceptanceResponse;
import com.rauio.smartdangjian.server.task.pojo.response.TaskResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TaskConvertor {

    TaskResponse toResponse(Task task);

    List<TaskResponse> toResponseList(List<Task> tasks);

    TaskAcceptanceResponse toAcceptanceResponse(TaskAcceptance acceptance);
}
