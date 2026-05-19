package com.rauio.smartdangjian.server.content.pojo.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PageVOTest {

    @Test
    @DisplayName("builder 构造 PageVO 所有字段值正确")
    void builderCreatesPageVOCorrectly() {
        List<String> data = List.of("item1", "item2");
        PageVO<String> pageVO = PageVO.<String>builder()
                .total(100L)
                .size(10L)
                .current(1L)
                .list(data)
                .build();

        assertThat(pageVO.getTotal()).isEqualTo(100L);
        assertThat(pageVO.getSize()).isEqualTo(10L);
        assertThat(pageVO.getCurrent()).isEqualTo(1L);
        assertThat(pageVO.getList()).hasSize(2);
        assertThat(pageVO.getList().get(0)).isEqualTo("item1");
    }

    @Test
    @DisplayName("builder 构造空列表 PageVO")
    void builderCreatesPageVOWithEmptyList() {
        PageVO<Object> pageVO = PageVO.builder()
                .total(0L)
                .size(10L)
                .current(1L)
                .list(List.of())
                .build();

        assertThat(pageVO.getTotal()).isEqualTo(0L);
        assertThat(pageVO.getList()).isEmpty();
    }

    @Test
    @DisplayName("toString 包含主要字段")
    void toStringContainsKeyFields() {
        PageVO<String> pageVO = PageVO.<String>builder()
                .total(50L)
                .size(10L)
                .current(3L)
                .list(List.of("a", "b"))
                .build();

        String str = pageVO.toString();

        assertThat(str).contains("50", "10", "3");
    }

    @Test
    @DisplayName("泛型 PageVO<Integer> 正常工作")
    void genericPageVOWithIntegers() {
        PageVO<Integer> pageVO = PageVO.<Integer>builder()
                .total(3L)
                .size(3L)
                .current(1L)
                .list(List.of(1, 2, 3))
                .build();

        assertThat(pageVO.getList().get(0)).isEqualTo(1);
        assertThat(pageVO.getList().get(2)).isEqualTo(3);
    }
}
