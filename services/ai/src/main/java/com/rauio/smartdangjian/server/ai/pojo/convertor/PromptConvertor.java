package com.rauio.smartdangjian.server.ai.pojo.convertor;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.rauio.smartdangjian.server.ai.pojo.entity.AiPrompts;
import com.rauio.smartdangjian.server.ai.pojo.response.AiPromptResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PromptConvertor {

    AiPromptResponse toResponse(AiPrompts aiPrompts);

    List<AiPromptResponse> toResponseList(List<AiPrompts> list);
}
