package com.rauio.smartdangjian.server.graph.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 党史知识图谱 schema 初始化器，幂等创建约束与索引。
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.neo4j.initialize-schema", havingValue = "true", matchIfMissing = true)
public class PartyHistoryGraphInitializer {

    private final Neo4jClient neo4jClient;

    @PostConstruct
    public void initializeSchema() {
        createUniqueConstraints();
        createPropertyIndexes();
        createFulltextIndex();
    }

    private void createUniqueConstraints() {
        String[] labels = {"Person", "Event", "Location", "Theory", "Document"};
        for (String label : labels) {
            String constraintName = label.toLowerCase() + "_graph_id_unique";
            String cypher = "CREATE CONSTRAINT " + constraintName
                    + " IF NOT EXISTS FOR (n:" + label + ") REQUIRE n.graph_id IS UNIQUE";
            try {
                neo4jClient.query(cypher).run();
                log.info("已确保唯一约束: {}", constraintName);
            } catch (Exception e) {
                log.warn("创建唯一约束 {} 失败: {}", constraintName, e.getMessage());
            }
        }
    }

    private void createPropertyIndexes() {
        // 索引定义: {索引名, 标签, 属性名}
        String[][] indexes = {
                {"person_name_idx", "Person", "name"},
                {"event_name_idx", "Event", "name"},
                {"event_start_date_idx", "Event", "startDate"},
                {"location_name_idx", "Location", "name"},
                {"theory_name_idx", "Theory", "name"},
                {"document_title_idx", "Document", "title"},
        };
        for (String[] idx : indexes) {
            String cypher = "CREATE INDEX " + idx[0] + " IF NOT EXISTS FOR (n:" + idx[1] + ") ON (n." + idx[2] + ")";
            try {
                neo4jClient.query(cypher).run();
                log.info("已确保属性索引: {}", idx[0]);
            } catch (Exception e) {
                log.warn("创建属性索引 {} 失败: {}", idx[0], e.getMessage());
            }
        }
    }

    private void createFulltextIndex() {
        String cypher = "CREATE FULLTEXT INDEX entity_search IF NOT EXISTS "
                + "FOR (n:Person|Event|Location|Theory|Document) "
                + "ON EACH [n.name, n.summary, n.title]";
        try {
            neo4jClient.query(cypher).run();
            log.info("已确保全文索引: entity_search");
        } catch (Exception e) {
            log.warn("创建全文索引 entity_search 失败: {}", e.getMessage());
        }
    }
}
