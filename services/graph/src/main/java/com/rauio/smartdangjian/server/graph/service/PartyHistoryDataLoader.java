package com.rauio.smartdangjian.server.graph.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spring.neo4j", name = "initialize-schema", havingValue = "true", matchIfMissing = true)
public class PartyHistoryDataLoader {

    private final PartyHistoryGraphService partyHistoryGraphService;
    private final Neo4jClient neo4jClient;
    private final ObjectMapper objectMapper;

    private static final String DATA_DIR = "party_history_data/";

    @PostConstruct
    public void initializePartyHistoryData() {
        try {
            if (hasExistingData()) {
                log.info("Neo4j 中已存在 Person 节点，跳过党史数据初始化");
                return;
            }

            int entityCount = 0;
            entityCount += loadEntities("entities_person.json", "Person");
            entityCount += loadEntities("entities_event.json", "Event");
            entityCount += loadEntities("entities_location.json", "Location");
            entityCount += loadEntities("entities_theory.json", "Theory");
            entityCount += loadEntities("entities_document.json", "Document");

            int relationshipCount = loadRelationships("relationships.json");

            log.info("党史数据导入完成：{} 个实体，{} 条关系", entityCount, relationshipCount);
        } catch (Exception e) {
            log.error("党史数据导入失败", e);
        }
    }

    private boolean hasExistingData() {
        try {
            return neo4jClient
                    .query("MATCH (n:Person) RETURN count(n) AS cnt")
                    .fetch()
                    .one()
                    .map(row -> row.get("cnt") instanceof Number num && num.longValue() > 0)
                    .orElse(false);
        } catch (Exception e) {
            log.debug("检查 Neo4j 已有数据时出错，按无数据处理: {}", e.getMessage());
            return false;
        }
    }

    private int loadEntities(String fileName, String label) throws Exception {
        List<Map<String, Object>> entities = readJsonFile(fileName);
        if (entities == null || entities.isEmpty()) {
            log.warn("种子数据文件 {} 为空或不存在，跳过 {} 导入", fileName, label);
            return 0;
        }
        partyHistoryGraphService.batchMergeEntities(label, entities);
        log.info("已导入 {} 个 {} 节点", entities.size(), label);
        return entities.size();
    }

    private int loadRelationships(String fileName) throws Exception {
        List<Map<String, Object>> relationships = readJsonFile(fileName);
        if (relationships == null || relationships.isEmpty()) {
            log.warn("种子数据文件 {} 为空或不存在，跳过关系导入", fileName);
            return 0;
        }
        partyHistoryGraphService.batchAddRelationships(relationships);
        log.info("已导入 {} 条关系", relationships.size());
        return relationships.size();
    }

    private List<Map<String, Object>> readJsonFile(String fileName) throws Exception {
        ClassPathResource resource = new ClassPathResource(DATA_DIR + fileName);
        if (!resource.exists()) {
            log.warn("classpath 上未找到文件: {}", DATA_DIR + fileName);
            return null;
        }
        try (InputStream is = resource.getInputStream()) {
            return objectMapper.readValue(is, new TypeReference<List<Map<String, Object>>>() {});
        }
    }
}
