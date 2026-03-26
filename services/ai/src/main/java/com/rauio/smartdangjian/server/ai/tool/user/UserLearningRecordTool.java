package com.rauio.smartdangjian.server.ai.tool.user;

import com.rauio.smartdangjian.server.ai.service.support.CurrentUserService;
import com.rauio.smartdangjian.server.ai.service.support.UserLearningRecordQueryService;
import com.rauio.smartdangjian.server.learning.pojo.dto.UserLearningRecordDto;
import com.rauio.smartdangjian.server.learning.pojo.vo.UserLearningRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiFunction;


@Component
@RequiredArgsConstructor
public class UserLearningRecordTool implements BiFunction<UserLearningRecordDto, ToolContext,String> {

    private final UserLearningRecordQueryService userLearningRecordQueryService;
    private final CurrentUserService currentUserService;

    @Override
    public String apply(
            @ToolParam(description = "需要获取的学习记录条数，默认10") UserLearningRecordDto s,
            ToolContext toolContext
    ) {
        List<UserLearningRecordVO> records = userLearningRecordQueryService.getByUserId(currentUserService.getCurrentUserId());
        records.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return records.toString();
    }
}
