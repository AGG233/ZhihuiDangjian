package com.rauio.smartdangjian.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

class TransactionConfigTest {

    private final TransactionConfig config = new TransactionConfig();

    @Test
    @DisplayName("dataSourceTransactionManager 创建 DataSourceTransactionManager 实例")
    void dataSourceTransactionManager() {
        DataSource dataSource = mock(DataSource.class);

        DataSourceTransactionManager manager =
                (DataSourceTransactionManager) config.dataSourceTransactionManager(dataSource);

        assertThat(manager).isNotNull();
        assertThat(manager.getDataSource()).isSameAs(dataSource);
    }
}
