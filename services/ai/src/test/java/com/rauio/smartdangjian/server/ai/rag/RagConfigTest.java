package com.rauio.smartdangjian.server.ai.rag;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neo4j.driver.Driver;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;

/**
 * {@link RagConfig} 的 @Bean 方法覆盖测试。
 *
 * <p>{@link Neo4jVectorStore} 的 schema 初始化（{@code initializeSchema(true)}）延迟到
 * {@code afterPropertiesSet()} 执行，而 {@code build()} 仅做字段装配、不连接 Neo4j，
 * 因此可直接用 mock 的 {@link Driver} 与 {@link EmbeddingModel} 调用 @Bean 方法验证构建成功。
 */
@ExtendWith(MockitoExtension.class)
class RagConfigTest {

    @Mock
    private Driver driver;

    @Mock
    private EmbeddingModel embeddingModel;

    private final RagConfig ragConfig = new RagConfig();

    @Test
    @DisplayName("neo4jVectorStore @Bean 构建成功且返回非空 Neo4jVectorStore")
    void neo4jVectorStoreBuildsSuccessfully() {
        Neo4jVectorStore store = ragConfig.neo4jVectorStore(driver, embeddingModel);

        assertThat(store).isNotNull();
    }
}
