package com.rauio.smartdangjian.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rauio.smartdangjian.config.SensitiveWordProperties;
import com.rauio.smartdangjian.pojo.response.SensitiveWordMatchResult;

@ExtendWith(MockitoExtension.class)
class SensitiveWordServiceTest {

    @Mock
    private SensitiveWordProperties properties;

    private SensitiveWordService service;

    @BeforeEach
    void setUp() {
        // Manually create properties with test values and initialize the service
        SensitiveWordProperties testProperties = new SensitiveWordProperties();
        testProperties.setEnabled(true);
        testProperties.setMaxLength(5000);
        testProperties.setEnableWhitelist(false);
        testProperties.setWhitelistLocation("classpath:sensitive-word/whitelist.txt");
        testProperties.setCustomDenyWords(List.of("反动", "色情", "暴力", "赌博", "毒品"));

        service = new SensitiveWordService(testProperties);
        service.afterPropertiesSet();
    }

    // ==================== check() tests ====================

    @Nested
    @DisplayName("check() - 敏感词检测")
    class CheckTests {

        @Test
        @DisplayName("空文本返回未匹配")
        void nullTextReturnsNotMatched() {
            SensitiveWordMatchResult result = service.check(null);

            assertThat(result.isMatched()).isFalse();
            assertThat(result.getWords()).isEmpty();
        }

        @Test
        @DisplayName("空白文本返回未匹配")
        void blankTextReturnsNotMatched() {
            SensitiveWordMatchResult result = service.check("   ");

            assertThat(result.isMatched()).isFalse();
            assertThat(result.getWords()).isEmpty();
        }

        @Test
        @DisplayName("无敏感词返回未匹配")
        void noSensitiveWordsReturnsNotMatched() {
            SensitiveWordMatchResult result = service.check("这是一段正常的文本内容");

            assertThat(result.isMatched()).isFalse();
            assertThat(result.getWords()).isEmpty();
            assertThat(result.getSanitizedText()).isEqualTo("这是一段正常的文本内容");
        }

        @Test
        @DisplayName("包含敏感词返回匹配")
        void containsSensitiveWordReturnsMatched() {
            SensitiveWordMatchResult result = service.check("这段文本包含反动内容");

            assertThat(result.isMatched()).isTrue();
            assertThat(result.getWords()).contains("反动");
            assertThat(result.getOriginalLength()).isEqualTo(10);
        }

        @Test
        @DisplayName("包含多个敏感词返回所有匹配词")
        void containsMultipleSensitiveWords() {
            SensitiveWordMatchResult result = service.check("反动色情暴力内容");

            assertThat(result.isMatched()).isTrue();
            assertThat(result.getWords()).contains("反动", "色情", "暴力");
        }

        @Test
        @DisplayName("白名单词汇（如毛泽东）不触发匹配")
        void whitelistWordsDoNotTriggerMatch() {
            // "毛泽东" is in the hardcoded whitelist
            SensitiveWordMatchResult result = service.check("毛泽东同志是伟大的领袖");

            assertThat(result.isMatched()).isFalse();
            assertThat(result.getWords()).isEmpty();
        }

        @Test
        @DisplayName("白名单词汇共产党不触发匹配")
        void whitelistWordCommunistPartyDoesNotTrigger() {
            SensitiveWordMatchResult result = service.check("中国共产党领导一切");

            assertThat(result.isMatched()).isFalse();
        }

        @Test
        @DisplayName("超长文本截断处理")
        void longTextIsTruncated() {
            // Create a new service with small maxLength
            SensitiveWordProperties shortProps = new SensitiveWordProperties();
            shortProps.setEnabled(true);
            shortProps.setMaxLength(10);
            shortProps.setEnableWhitelist(false);
            shortProps.setCustomDenyWords(List.of("反动"));

            SensitiveWordService shortService = new SensitiveWordService(shortProps);
            shortService.afterPropertiesSet();

            // Text longer than 10 chars, "反动" appears after position 10
            String longText = "这是一段很长的文本反动内容";
            SensitiveWordMatchResult result = shortService.check(longText);

            // "反动" is at position 10 (0-indexed), after truncation to 10 chars it should not match
            assertThat(result.isMatched()).isFalse();
            assertThat(result.getOriginalLength()).isEqualTo(longText.length());
        }

        @Test
        @DisplayName("敏感词在截断范围内仍被检测到")
        void sensitiveWordWithinTruncationRange() {
            SensitiveWordProperties shortProps = new SensitiveWordProperties();
            shortProps.setEnabled(true);
            shortProps.setMaxLength(20);
            shortProps.setEnableWhitelist(false);
            shortProps.setCustomDenyWords(List.of("反动"));

            SensitiveWordService shortService = new SensitiveWordService(shortProps);
            shortService.afterPropertiesSet();

            String text = "这段文本有反动内容";
            SensitiveWordMatchResult result = shortService.check(text);

            assertThat(result.isMatched()).isTrue();
            assertThat(result.getWords()).contains("反动");
        }
    }

    // ==================== containsSensitiveWord() tests ====================

    @Nested
    @DisplayName("containsSensitiveWord() - 判断是否包含敏感词")
    class ContainsTests {

        @Test
        @DisplayName("包含敏感词返回 true")
        void containsReturnsTrue() {
            assertThat(service.containsSensitiveWord("这段文本包含反动内容")).isTrue();
        }

        @Test
        @DisplayName("不包含敏感词返回 false")
        void notContainsReturnsFalse() {
            assertThat(service.containsSensitiveWord("这是一段正常的文本")).isFalse();
        }

        @Test
        @DisplayName("空文本返回 false")
        void nullTextReturnsFalse() {
            assertThat(service.containsSensitiveWord(null)).isFalse();
        }

        @Test
        @DisplayName("白名单词汇返回 false")
        void whitelistWordReturnsFalse() {
            assertThat(service.containsSensitiveWord("毛泽东")).isFalse();
        }
    }

    // ==================== sanitize() tests ====================

    @Nested
    @DisplayName("sanitize() - 替换敏感词")
    class SanitizeTests {

        @Test
        @DisplayName("替换敏感词")
        void replacesSensitiveWords() {
            SensitiveWordMatchResult result = service.sanitize("这段文本包含反动内容");

            assertThat(result.isMatched()).isTrue();
            assertThat(result.getSanitizedText()).doesNotContain("反动");
            assertThat(result.getWords()).contains("反动");
        }

        @Test
        @DisplayName("无敏感词时原文返回")
        void noSensitiveWordsReturnsOriginal() {
            String text = "这是一段正常的文本";
            SensitiveWordMatchResult result = service.sanitize(text);

            assertThat(result.isMatched()).isFalse();
            assertThat(result.getSanitizedText()).isEqualTo(text);
        }

        @Test
        @DisplayName("空文本返回空结果")
        void nullTextReturnsEmptyResult() {
            SensitiveWordMatchResult result = service.sanitize(null);

            assertThat(result.isMatched()).isFalse();
        }
    }

    // ==================== findWords() tests ====================

    @Nested
    @DisplayName("findWords() - 查找所有命中词")
    class FindWordsTests {

        @Test
        @DisplayName("返回所有命中词")
        void returnsAllMatchedWords() {
            List<String> words = service.findWords("反动色情暴力赌博毒品");

            assertThat(words).contains("反动", "色情", "暴力", "赌博", "毒品");
        }

        @Test
        @DisplayName("无命中返回空列表")
        void noMatchReturnsEmptyList() {
            List<String> words = service.findWords("这是一段正常的文本");

            assertThat(words).isEmpty();
        }

        @Test
        @DisplayName("空文本返回空列表")
        void nullTextReturnsEmptyList() {
            List<String> words = service.findWords(null);

            assertThat(words).isEmpty();
        }

        @Test
        @DisplayName("白名单词汇不在结果中")
        void whitelistWordsNotInResult() {
            List<String> words = service.findWords("毛泽东共产党社会主义");

            assertThat(words).isEmpty();
        }
    }
}
