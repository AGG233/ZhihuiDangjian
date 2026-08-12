package com.rauio.smartdangjian.server.content.pojo.request;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PageRequestTest {

    @Test
    @DisplayName("setter 设置 pageNum 和 pageSize 后 getter 返回正确值")
    void settersAndGettersWorkCorrectly() {
        PageRequest request = new PageRequest();
        request.setPageNum(1);
        request.setPageSize(10);

        assertThat(request.getPageNum()).isEqualTo(1);
        assertThat(request.getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("toString 包含 pageNum 和 pageSize")
    void toStringContainsFields() {
        PageRequest request = new PageRequest();
        request.setPageNum(2);
        request.setPageSize(20);

        String str = request.toString();

        assertThat(str).contains("2", "20");
    }

    @Test
    @DisplayName("默认值 pageNum 和 pageSize 为 0")
    void defaultValuesAreZero() {
        PageRequest request = new PageRequest();

        assertThat(request.getPageNum()).isEqualTo(0);
        assertThat(request.getPageSize()).isEqualTo(0);
    }
}
