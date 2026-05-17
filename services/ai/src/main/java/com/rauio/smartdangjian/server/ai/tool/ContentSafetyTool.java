package com.rauio.smartdangjian.server.ai.tool;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ContentSafetyTool {

    @Tool(name = "checkContentSafety", description = "检查文本内容是否包含敏感信息或不合规内容，返回分析结果")
    public Map<String, Object> checkContentSafety(@ToolParam(description = "需要检查的文本内容") String content) {
        Map<String, Object> result = new HashMap<>();
        result.put("contentLength", content != null ? content.length() : 0);
        result.put("reviewRequired", true);
        result.put("note", "内容安全审查结果由审查Agent根据系统提示词和合规标准进行分析判断");
        return result;
    }
}
