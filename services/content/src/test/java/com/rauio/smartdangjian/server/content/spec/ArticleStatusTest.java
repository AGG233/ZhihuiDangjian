package com.rauio.smartdangjian.server.content.spec;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ArticleStatusTest {

    @Test
    @DisplayName("所有 ArticleStatus 常量 status 字段不为空")
    void allArticleStatusesHaveStatus() {
        for (ArticleStatus as : ArticleStatus.values()) {
            assertThat(as.getStatus()).isNotNull();
        }
    }

    @Test
    @DisplayName("ArticleStatus 枚举值正确")
    void articleStatusValues() {
        assertThat(ArticleStatus.valueOf("Draft")).isEqualTo(ArticleStatus.Draft);
        assertThat(ArticleStatus.valueOf("Published")).isEqualTo(ArticleStatus.Published);
        assertThat(ArticleStatus.valueOf("Deleted")).isEqualTo(ArticleStatus.Deleted);
    }
}
