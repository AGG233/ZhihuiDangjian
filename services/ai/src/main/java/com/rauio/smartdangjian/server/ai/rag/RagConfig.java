package com.rauio.smartdangjian.server.ai.rag;

import org.neo4j.driver.Driver;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.neo4j.Neo4jVectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RAG 向量存储配置。
 *
 * <p>基于 DashScope text-embedding-v3（1024 维）与 Neo4j 构建向量索引，
 * 供文档入库与后续向量检索使用。索引与约束通过 {@code initializeSchema(true)}
 * 由 Neo4jVectorStore 在初始化时自动创建（禁止手写自定义索引 DDL）。</p>
 */
@Configuration
public class RagConfig {

    @Bean
    public Neo4jVectorStore neo4jVectorStore(Driver driver, EmbeddingModel embeddingModel) {
        return Neo4jVectorStore.builder(driver, embeddingModel)
                .databaseName("neo4j")
                .distanceType(Neo4jVectorStore.Neo4jDistanceType.COSINE)
                .embeddingDimension(1024) // DashScope text-embedding-v3 维度
                .label("Document")
                .embeddingProperty("embedding")
                .indexName("document-vector-index")
                .initializeSchema(true)
                .build();
    }
}
