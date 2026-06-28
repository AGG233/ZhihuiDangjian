package com.rauio.smartdangjian.server.ai.tool;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

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

    @Test
    @DisplayName("checkContentSafety 空字符串时 contentLength 返回 0 且 reviewRequired 为 true")
    void checkContentSafetyEmptyString() {
        Map<String, Object> result = contentSafetyTool.checkContentSafety("");

        assertThat(result).containsEntry("contentLength", 0);
        assertThat(result).containsEntry("reviewRequired", true);
    }

    @Test
    @DisplayName("checkContentSafety 长文本时 contentLength 正确反映字符数")
    void checkContentSafetyLongContent() {
        String longContent = "A".repeat(1000);

        Map<String, Object> result = contentSafetyTool.checkContentSafety(longContent);

        assertThat(result).containsEntry("contentLength", 1000);
    }

    @Test
    @DisplayName("checkContentSafety 特殊字符时 contentLength 正确反映字符数")
    void checkContentSafetySpecialCharacters() {
        String specialContent = "!@#$%^&*()_+{}:\"<>?|[];',./~`你好123";

        Map<String, Object> result = contentSafetyTool.checkContentSafety(specialContent);

        assertThat(result).containsEntry("contentLength", specialContent.length());
        assertThat(result).containsEntry("reviewRequired", true);
    }

    @Test
    @DisplayName("checkContentSafety 始终返回固定 note 信息说明审查方式")
    void checkContentSafetyReturnsFixedNote() {
        Map<String, Object> result = contentSafetyTool.checkContentSafety("test");

        assertThat(result).containsEntry("note", "内容安全审查结果由审查Agent根据系统提示词和合规标准进行分析判断");
    }

    @Test
    @DisplayName("checkContentSafety 纯空白字符串时 contentLength 计算空白字符")
    void checkContentSafetyWhitespace() {
        Map<String, Object> result = contentSafetyTool.checkContentSafety("   ");

        assertThat(result).containsEntry("contentLength", 3);
        assertThat(result).containsEntry("reviewRequired", true);
    }
}
