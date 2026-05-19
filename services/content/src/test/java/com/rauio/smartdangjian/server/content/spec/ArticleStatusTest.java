package com.rauio.smartdangjian.server.content.spec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ArticleStatusTest {

    @Test
    @DisplayName("Draft 状态值为 draft")
    void draftStatusValue() {
        assertThat(ArticleStatus.Draft.getStatus()).isEqualTo("draft");
    }

    @Test
    @DisplayName("Published 状态值为 published")
    void publishedStatusValue() {
        assertThat(ArticleStatus.Published.getStatus()).isEqualTo("published");
    }

    @Test
    @DisplayName("Deleted 状态值为 archived")
    void deletedStatusValue() {
        assertThat(ArticleStatus.Deleted.getStatus()).isEqualTo("archived");
    }

    @Test
    @DisplayName("valueOf Draft 返回正确的枚举实例")
    void valueOfDraft() {
        assertThat(ArticleStatus.valueOf("Draft")).isEqualTo(ArticleStatus.Draft);
    }

    @Test
    @DisplayName("valueOf Published 返回正确的枚举实例")
    void valueOfPublished() {
        assertThat(ArticleStatus.valueOf("Published")).isEqualTo(ArticleStatus.Published);
    }

    @Test
    @DisplayName("valueOf Deleted 返回正确的枚举实例")
    void valueOfDeleted() {
        assertThat(ArticleStatus.valueOf("Deleted")).isEqualTo(ArticleStatus.Deleted);
    }

    @Test
    @DisplayName("枚举 values 长度为 3")
    void enumValuesLength() {
        assertThat(ArticleStatus.values()).hasSize(3);
    }
}
