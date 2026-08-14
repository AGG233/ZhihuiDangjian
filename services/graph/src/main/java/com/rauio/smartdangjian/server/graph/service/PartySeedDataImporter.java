package com.rauio.smartdangjian.server.graph.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.neo4j.core.Neo4jClient;
import org.springframework.stereotype.Service;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.graph.constants.GraphErrorConstants;
import com.rauio.smartdangjian.server.graph.pojo.response.PartySeedImportResponse;

import lombok.RequiredArgsConstructor;

/**
 * 党史种子数据导入器：读取 classpath 下 seed/ 目录的 CSV 种子数据，
 * 按 T10 {@link PartyGraphService} 的节点/关系定义以参数化 MERGE 幂等写入 Neo4j。
 *
 * <p>不依赖 Neo4j import 目录的 LOAD CSV 配置，全部通过 {@link Neo4jClient}
 * 参数化绑定执行。不自动运行（无 {@code @PostConstruct}），由 Admin 接口触发
 * {@link #importAll()}。
 */
@Service
@RequiredArgsConstructor
public class PartySeedDataImporter {

    /** Person-PARTICIPATED_IN->Event 关系 MERGE（幂等） */
    private static final String PERSON_EVENT_CYPHER = "MERGE (p:Person {id:$personId})\n"
            + "MERGE (e:Event {id:$eventId})\n"
            + "MERGE (p)-[:PARTICIPATED_IN]->(e)";

    /** Document-RELATED_TO->Theory 关系 MERGE（顺带补齐 Document 节点属性） */
    private static final String DOCUMENT_THEORY_CYPHER = "MERGE (d:Document {id:$documentId})\n"
            + "SET d.name = $documentName, d.description = $documentDescription, "
            + "d.publisher = $documentPublisher, d.date = $documentDate\n"
            + "MERGE (t:Theory {id:$theoryId})\n"
            + "MERGE (d)-[:RELATED_TO]->(t)";

    private final PartyGraphService partyGraphService;
    private final Neo4jClient neo4jClient;

    /**
     * 导入全部党史种子数据（节点 + 关系），返回各类导入计数。
     *
     * @return 导入结果计数
     */
    public PartySeedImportResponse importAll() {
        int personCount = importPersons();
        int eventCount = importEvents();
        int theoryCount = importTheories();
        int personEventCount = importPersonEvents();
        int documentTheoryCount = importDocumentTheories();
        return PartySeedImportResponse.builder()
                .personCount(personCount)
                .eventCount(eventCount)
                .theoryCount(theoryCount)
                .personEventCount(personEventCount)
                .documentTheoryCount(documentTheoryCount)
                .total(personCount + eventCount + theoryCount + personEventCount + documentTheoryCount)
                .build();
    }

    /**
     * 导入 Person 节点（persons.csv）。
     *
     * @return 导入行数
     */
    private int importPersons() {
        int count = 0;
        for (List<String> row : readSeedCsv("seed/persons.csv")) {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("id", row.get(0));
            props.put("name", row.get(1));
            props.put("description", row.get(2));
            props.put("birthYear", parseIntOrNull(row.get(3)));
            props.put("deathYear", parseIntOrNull(row.get(4)));
            partyGraphService.upsertPartyEntity("Person", props);
            count++;
        }
        return count;
    }

    /**
     * 导入 Event 节点（events.csv）。
     *
     * @return 导入行数
     */
    private int importEvents() {
        int count = 0;
        for (List<String> row : readSeedCsv("seed/events.csv")) {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("id", row.get(0));
            props.put("name", row.get(1));
            props.put("description", row.get(2));
            props.put("date", row.get(3));
            props.put("location", row.get(4));
            partyGraphService.upsertPartyEntity("Event", props);
            count++;
        }
        return count;
    }

    /**
     * 导入 Theory 节点（theories.csv）。
     *
     * @return 导入行数
     */
    private int importTheories() {
        int count = 0;
        for (List<String> row : readSeedCsv("seed/theories.csv")) {
            Map<String, Object> props = new LinkedHashMap<>();
            props.put("id", row.get(0));
            props.put("name", row.get(1));
            props.put("description", row.get(2));
            props.put("era", row.get(3));
            partyGraphService.upsertPartyEntity("Theory", props);
            count++;
        }
        return count;
    }

    /**
     * 导入 Person-PARTICIPATED_IN->Event 关系（person_event.csv）。
     *
     * @return 导入行数
     */
    private int importPersonEvents() {
        int count = 0;
        for (List<String> row : readSeedCsv("seed/person_event.csv")) {
            neo4jClient
                    .query(PERSON_EVENT_CYPHER)
                    .bind(row.get(0))
                    .to("personId")
                    .bind(row.get(1))
                    .to("eventId")
                    .run();
            count++;
        }
        return count;
    }

    /**
     * 导入 Document-RELATED_TO->Theory 关系（document_theory.csv）。
     *
     * @return 导入行数
     */
    private int importDocumentTheories() {
        int count = 0;
        for (List<String> row : readSeedCsv("seed/document_theory.csv")) {
            neo4jClient
                    .query(DOCUMENT_THEORY_CYPHER)
                    .bind(row.get(0))
                    .to("documentId")
                    .bind(row.get(1))
                    .to("documentName")
                    .bind(row.get(2))
                    .to("documentDescription")
                    .bind(row.get(3))
                    .to("documentPublisher")
                    .bind(row.get(4))
                    .to("documentDate")
                    .bind(row.get(5))
                    .to("theoryId")
                    .run();
            count++;
        }
        return count;
    }

    /**
     * 读取 seed 目录下指定 CSV 资源，跳过表头与空行，返回数据行。
     *
     * @param path classpath 下相对路径，如 seed/persons.csv
     * @return 每行按逗号切分后的字段列表
     */
    private List<List<String>> readSeedCsv(String path) {
        Resource resource = new ClassPathResource(path);
        List<List<String>> rows = new ArrayList<>();
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            boolean header = true;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                if (header) {
                    header = false;
                    continue;
                }
                rows.add(parseCsvLine(line));
            }
            return rows;
        } catch (IOException e) {
            throw new BusinessException(GraphErrorConstants.PARTY_SEED_READ_FAILED, "读取党史种子数据失败: " + path);
        }
    }

    /**
     * 解析单行 CSV（支持双引号包裹字段与 "" 转义）。
     *
     * @param line CSV 行
     * @return 字段列表
     */
    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        current.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    current.append(c);
                }
            } else if (c == '"') {
                inQuotes = true;
            } else if (c == ',') {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields;
    }

    /**
     * 将字符串解析为整数，空串返回 null。
     *
     * @param value 字符串值
     * @return 整数或 null
     */
    private Integer parseIntOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return Integer.valueOf(value.trim());
    }
}
