package com.rauio.smartdangjian.server.graph.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.neo4j.core.Neo4jClient;

import com.rauio.smartdangjian.server.graph.pojo.response.PartySeedImportResponse;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PartySeedDataImporterTest {

    @Mock
    private PartyGraphService partyGraphService;

    @Mock
    private Neo4jClient neo4jClient;

    @InjectMocks
    private PartySeedDataImporter partySeedDataImporter;

    /** Neo4jClient 关系写入链 mock：query → bind → to → run */
    private final class Chain {
        final Neo4jClient.UnboundRunnableSpec spec = mock(Neo4jClient.UnboundRunnableSpec.class);
        final Neo4jClient.OngoingBindSpec bindSpec = mock(Neo4jClient.OngoingBindSpec.class);

        private Chain() {
            lenient().when(neo4jClient.query(anyString())).thenReturn(spec);
            lenient().when(spec.bind(any())).thenReturn(bindSpec);
            lenient().when(bindSpec.to(anyString())).thenReturn(spec);
        }
    }

    /** 统计种子 CSV 数据行数（跳过表头与空行），与导入器读到的行数一致 */
    private int countDataRows(String path) throws IOException {
        ClassPathResource resource = new ClassPathResource(path);
        try (BufferedReader reader =
                new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            int count = 0;
            boolean header = true;
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                if (header) {
                    header = false;
                    continue;
                }
                count++;
            }
            return count;
        }
    }

    @Test
    @DisplayName("importAll 节点 MERGE 次数与各节点 CSV 行数一致")
    void importAllMergesNodesMatchingCsvRowCount() throws IOException {
        new Chain();

        PartySeedImportResponse result = partySeedDataImporter.importAll();

        int personCount = countDataRows("seed/persons.csv");
        int eventCount = countDataRows("seed/events.csv");
        int theoryCount = countDataRows("seed/theories.csv");

        verify(partyGraphService, times(personCount)).upsertPartyEntity(eq("Person"), any());
        verify(partyGraphService, times(eventCount)).upsertPartyEntity(eq("Event"), any());
        verify(partyGraphService, times(theoryCount)).upsertPartyEntity(eq("Theory"), any());

        assertThat(result.getPersonCount()).isEqualTo(personCount);
        assertThat(result.getEventCount()).isEqualTo(eventCount);
        assertThat(result.getTheoryCount()).isEqualTo(theoryCount);
    }

    @Test
    @DisplayName("importAll 关系 MERGE 次数与关系 CSV 行数一致且生成对应 Cypher")
    void importAllMergesRelationsMatchingCsvRowCount() throws IOException {
        Chain chain = new Chain();

        partySeedDataImporter.importAll();

        int personEventCount = countDataRows("seed/person_event.csv");
        int documentTheoryCount = countDataRows("seed/document_theory.csv");

        ArgumentCaptor<String> cypherCaptor = ArgumentCaptor.forClass(String.class);
        verify(neo4jClient, times(personEventCount + documentTheoryCount)).query(cypherCaptor.capture());
        List<String> cyphers = cypherCaptor.getAllValues();

        List<String> personEventCyphers = cyphers.subList(0, personEventCount);
        List<String> documentTheoryCyphers = cyphers.subList(personEventCount, cyphers.size());

        assertThat(personEventCyphers).allSatisfy(c -> assertThat(c)
                .contains("MERGE (p:Person {id:$personId})")
                .contains("MERGE (e:Event {id:$eventId})")
                .contains("MERGE (p)-[:PARTICIPATED_IN]->(e)"));
        assertThat(documentTheoryCyphers).allSatisfy(c -> assertThat(c)
                .contains("MERGE (d:Document {id:$documentId})")
                .contains("d.name = $documentName")
                .contains("MERGE (t:Theory {id:$theoryId})")
                .contains("MERGE (d)-[:RELATED_TO]->(t)"));

        verify(chain.spec, times(personEventCount + documentTheoryCount)).run();
    }

    @Test
    @DisplayName("importAll 返回总计数等于全部 CSV 数据行数之和")
    void importAllReturnsTotalMatchingAllCsvRowCount() throws IOException {
        new Chain();

        PartySeedImportResponse result = partySeedDataImporter.importAll();

        int personEventCount = countDataRows("seed/person_event.csv");
        int documentTheoryCount = countDataRows("seed/document_theory.csv");
        int total = countDataRows("seed/persons.csv")
                + countDataRows("seed/events.csv")
                + countDataRows("seed/theories.csv")
                + personEventCount
                + documentTheoryCount;

        assertThat(result.getPersonEventCount()).isEqualTo(personEventCount);
        assertThat(result.getDocumentTheoryCount()).isEqualTo(documentTheoryCount);
        assertThat(result.getTotal()).isEqualTo(total);
    }
}
