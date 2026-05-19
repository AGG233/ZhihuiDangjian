package com.rauio.smartdangjian.aop.support;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DataScopeActionTest {

    @Test
    @DisplayName("DataScopeAction 包含 6 个枚举常量")
    void constants() {
        assertThat(DataScopeAction.values()).hasSize(6);
    }

    @Test
    @DisplayName("READ 常量存在")
    void read() {
        assertThat(DataScopeAction.READ).isEqualTo(DataScopeAction.valueOf("READ"));
    }

    @Test
    @DisplayName("SEARCH 常量存在")
    void search() {
        assertThat(DataScopeAction.SEARCH).isEqualTo(DataScopeAction.valueOf("SEARCH"));
    }

    @Test
    @DisplayName("CREATE 常量存在")
    void create() {
        assertThat(DataScopeAction.CREATE).isEqualTo(DataScopeAction.valueOf("CREATE"));
    }

    @Test
    @DisplayName("UPDATE 常量存在")
    void update() {
        assertThat(DataScopeAction.UPDATE).isEqualTo(DataScopeAction.valueOf("UPDATE"));
    }

    @Test
    @DisplayName("DELETE 常量存在")
    void delete() {
        assertThat(DataScopeAction.DELETE).isEqualTo(DataScopeAction.valueOf("DELETE"));
    }

    @Test
    @DisplayName("FILTER 常量存在")
    void filter() {
        assertThat(DataScopeAction.FILTER).isEqualTo(DataScopeAction.valueOf("FILTER"));
    }
}
