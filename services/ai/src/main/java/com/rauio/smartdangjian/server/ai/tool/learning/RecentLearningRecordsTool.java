package com.rauio.smartdangjian.server.ai.tool.learning;

import com.rauio.smartdangjian.server.ai.service.support.CurrentUserService;
import com.rauio.smartdangjian.server.ai.service.support.UserLearningRecordQueryService;
import com.rauio.smartdangjian.server.learning.pojo.dto.UserLearningRecordDto;
import com.rauio.smartdangjian.server.learning.pojo.vo.UserLearningRecordVO;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiFunction;

@Component
@RequiredArgsConstructor
public class RecentLearningRecordsTool implements BiFunction<UserLearningRecordDto, ToolContext,List<UserLearningRecordVO>> {

    private final CurrentUserService currentUserService;
    private final UserLearningRecordQueryService userLearningRecordQueryService;

    public List<UserLearningRecordVO> apply(
            @ToolParam(description = "需要获取的学习记录条数，默认10") UserLearningRecordDto dto,
            ToolContext toolContext
    ) {
        List<UserLearningRecordVO> records = userLearningRecordQueryService.getByUserId(currentUserService.getCurrentUserId());
        records.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        return records;
    }
}
