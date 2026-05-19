package com.rauio.smartdangjian.server.content.pojo.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PageResponseTest {

    @Test
    @DisplayName("builder 构造 PageResponse 所有字段值正确")
    void builderCreatesPageResponseCorrectly() {
        List<String> data = List.of("item1", "item2");
        PageResponse<String> pageResponse = PageResponse.<String>builder()
                .total(100L)
                .size(10L)
                .current(1L)
                .list(data)
                .build();

        assertThat(pageResponse.getTotal()).isEqualTo(100L);
        assertThat(pageResponse.getSize()).isEqualTo(10L);
        assertThat(pageResponse.getCurrent()).isEqualTo(1L);
        assertThat(pageResponse.getList()).hasSize(2);
        assertThat(pageResponse.getList().get(0)).isEqualTo("item1");
    }

    @Test
    @DisplayName("builder 构造空列表 PageResponse")
    void builderCreatesPageResponseWithEmptyList() {
        PageResponse<Object> pageResponse = PageResponse.builder()
                .total(0L)
                .size(10L)
                .current(1L)
                .list(List.of())
                .build();

        assertThat(pageResponse.getTotal()).isEqualTo(0L);
        assertThat(pageResponse.getList()).isEmpty();
    }

    @Test
    @DisplayName("toString 包含主要字段")
    void toStringContainsKeyFields() {
        PageResponse<String> pageResponse = PageResponse.<String>builder()
                .total(50L)
                .size(10L)
                .current(3L)
                .list(List.of("a", "b"))
                .build();

        String str = pageResponse.toString();

        assertThat(str).contains("50", "10", "3");
    }

    @Test
    @DisplayName("泛型 PageResponse<Integer> 正常工作")
    void genericPageResponseWithIntegers() {
        PageResponse<Integer> pageResponse = PageResponse.<Integer>builder()
                .total(3L)
                .size(3L)
                .current(1L)
                .list(List.of(1, 2, 3))
                .build();

        assertThat(pageResponse.getList().get(0)).isEqualTo(1);
        assertThat(pageResponse.getList().get(2)).isEqualTo(3);
    }
}
