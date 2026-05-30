package com.rauio.smartdangjian.server.graph.service;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartyHistoryImportService {

    private final PartyHistoryGraphService partyHistoryGraphService;

    /**
     * 批量导入实体节点。
     *
     * @param label    节点标签，如 Person、Event、Location、Theory、Document
     * @param entities 实体属性列表，每个 Map 的 graph_id 用于唯一标识节点
     * @return 导入的实体数量
     */
    public int importEntities(String label, List<Map<String, Object>> entities) {
        if (entities == null || entities.isEmpty()) {
            log.warn("实体列表为空，跳过 {} 导入", label);
            return 0;
        }
        partyHistoryGraphService.batchMergeEntities(label, entities);
        log.info("通过 PartyHistoryImportService 导入 {} 个 {} 节点", entities.size(), label);
        return entities.size();
    }

    /**
     * 批量导入关系。
     *
     * @param relationships 关系列表，每个 Map 包含 source、target、type、properties 字段
     * @return 导入的关系数量
     */
    public int importRelationships(List<Map<String, Object>> relationships) {
        if (relationships == null || relationships.isEmpty()) {
            log.warn("关系列表为空，跳过关系导入");
            return 0;
        }
        partyHistoryGraphService.batchAddRelationships(relationships);
        log.info("通过 PartyHistoryImportService 导入 {} 条关系", relationships.size());
        return relationships.size();
    }
}
