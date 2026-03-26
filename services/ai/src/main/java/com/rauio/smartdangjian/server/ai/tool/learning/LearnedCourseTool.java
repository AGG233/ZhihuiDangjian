package com.rauio.smartdangjian.server.ai.tool.learning;

import com.rauio.smartdangjian.server.ai.service.support.CurrentUserService;
import com.rauio.smartdangjian.server.ai.service.support.UserLearningRecordQueryService;
import com.rauio.smartdangjian.server.learning.pojo.dto.UserLearningRecordDto;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiFunction;

@Component
@RequiredArgsConstructor
public class LearnedCourseTool implements BiFunction<UserLearningRecordDto, ToolContext, List<String>> {

    private final CurrentUserService currentUserService;
    private final UserLearningRecordQueryService userLearningRecordQueryService;


    @Override
    public List<String> apply(UserLearningRecordDto dto, ToolContext toolContext) {
        return userLearningRecordQueryService.getLearnedCourseIdsByUserId(currentUserService.getCurrentUserId());
    }
}
