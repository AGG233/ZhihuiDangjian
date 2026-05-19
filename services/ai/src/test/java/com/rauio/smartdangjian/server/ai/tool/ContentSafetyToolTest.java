package com.rauio.smartdangjian.server.ai.tool;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ContentSafetyToolTest {

    @InjectMocks
    private ContentSafetyTool contentSafetyTool;

    @Test
    @DisplayName("checkContentSafety 返回内容长度和审查标记")
    void checkContentSafety() {
        Map<String, Object> result = contentSafetyTool.checkContentSafety("需要检查的内容");

        assertThat(result).containsEntry("contentLength", 7);
        assertThat(result).containsEntry("reviewRequired", true);
        assertThat(result).containsKey("note");
    }

    @Test
    @DisplayName("checkContentSafety 内容为 null 时 contentLength 返回 0")
    void checkContentSafetyNullContent() {
        Map<String, Object> result = contentSafetyTool.checkContentSafety(null);

        assertThat(result).containsEntry("contentLength", 0);
    }
}
