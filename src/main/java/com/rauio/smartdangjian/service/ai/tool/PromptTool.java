package com.rauio.smartdangjian.service.ai.tool;

import com.rauio.smartdangjian.pojo.AiSystemPrompt;
import com.rauio.smartdangjian.pojo.request.AiPromptCreateRequest;
import com.rauio.smartdangjian.service.ai.PromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PromptTool {

    private final PromptService promptService;

    @Tool(name = "saveSystemPrompt", description = "向Redis中存入系统提示词")
    public AiSystemPrompt saveSystemPrompt(
            @ToolParam(description = "提示词类型: COMMON 或 EVALUATION") String type,
            @ToolParam(description = "提示词内容") String content,
            @ToolParam(description = "是否启用, 默认true") Boolean enabled,
            @ToolParam(description = "排序号(升序), 默认0") Integer sort
    ) {
        AiPromptCreateRequest request = new AiPromptCreateRequest();
        request.setType(type);
        request.setContent(content);
        request.setEnabled(enabled);
        request.setSort(sort);
        return promptService.create(request);
    }
}
