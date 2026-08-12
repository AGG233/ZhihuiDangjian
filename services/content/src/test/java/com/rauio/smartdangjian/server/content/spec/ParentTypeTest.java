package com.rauio.smartdangjian.server.content.spec;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ParentTypeTest {

    @Test
    @DisplayName("chapter 类型值为 chapter")
    void chapterTypeValue() {
        assertThat(ParentType.chapter.getType()).isEqualTo("chapter");
    }

    @Test
    @DisplayName("article 类型值为 article")
    void articleTypeValue() {
        assertThat(ParentType.article.getType()).isEqualTo("article");
    }

    @Test
    @DisplayName("枚举 values 长度为 2")
    void enumValuesLength() {
        assertThat(ParentType.values()).hasSize(2);
    }

    @Test
    @DisplayName("valueOf chapter 返回正确的枚举实例")
    void valueOfChapter() {
        assertThat(ParentType.valueOf("chapter")).isEqualTo(ParentType.chapter);
    }

    @Test
    @DisplayName("valueOf article 返回正确的枚举实例")
    void valueOfArticle() {
        assertThat(ParentType.valueOf("article")).isEqualTo(ParentType.article);
    }
}
