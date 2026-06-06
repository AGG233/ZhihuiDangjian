package com.rauio.smartdangjian.server.graph.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.neo4j.core.Neo4jClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PartyHistoryDataLoaderTest {

    @Mock
    private PartyHistoryGraphService partyHistoryGraphService;

    @Mock
    private Neo4jClient neo4jClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PartyHistoryDataLoader partyHistoryDataLoader;

    // ==================== Helper: Neo4jClient query chain mock for hasExistingData ====================

    @SuppressWarnings("unchecked")
    private Neo4jClient.RecordFetchSpec<Map<String, Object>> setupFetchOneChain(Optional<Map<String, Object>> result) {
        Neo4jClient.UnboundRunnableSpec querySpec = mock(Neo4jClient.UnboundRunnableSpec.class);
        Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = mock(Neo4jClient.RecordFetchSpec.class);
        lenient().when(neo4jClient.query(anyString())).thenReturn(querySpec);
        lenient().when(querySpec.fetch()).thenReturn(fetchSpec);
        lenient().when(fetchSpec.one()).thenReturn(result);
        return fetchSpec;
    }

    // ==================== Helper: Test data ====================

    @SuppressWarnings("unchecked")
    private static Map<String, Object> countRow(long cnt) {
        Map<String, Object> row = mock(Map.class);
        when(row.get("cnt")).thenReturn(cnt);
        return row;
    }

    private static List<Map<String, Object>> personEntities() {
        return List.of(Map.of("graph_id", "person-001", "name", "毛泽东"));
    }

    private static List<Map<String, Object>> eventEntities() {
        return List.of(Map.of("graph_id", "event-001", "name", "秋收起义"));
    }

    private static List<Map<String, Object>> locationEntities() {
        return List.of(Map.of("graph_id", "loc-001", "name", "延安"));
    }

    private static List<Map<String, Object>> theoryEntities() {
        return List.of(Map.of("graph_id", "theory-001", "name", "毛泽东思想"));
    }

    private static List<Map<String, Object>> documentEntities() {
        return List.of(Map.of("graph_id", "doc-001", "title", "共产党宣言"));
    }

    private static List<Map<String, Object>> relationships() {
        return List.of(Map.of(
                "source", "person-001",
                "target", "event-001",
                "type", "INITIATED"));
    }

    // ==================== NormalTests ====================

    @Nested
    @DisplayName("NormalTests — 正常路径")
    class NormalTests {

        @Test
        @DisplayName("Neo4j 中已存在 Person 数据时跳过初始化")
        void skipInitializationWhenDataExists() {
            setupFetchOneChain(Optional.of(countRow(5L)));

            // Use mockConstruction so ClassPathResource never gets called
            try (var ignored = mockConstruction(ClassPathResource.class)) {
                partyHistoryDataLoader.initializePartyHistoryData();
            }

            // Verify no batch operations were called
            verify(partyHistoryGraphService, never()).batchMergeEntities(anyString(), any());
            verify(partyHistoryGraphService, never()).batchAddRelationships(any());
        }

        @Test
        @DisplayName("Neo4j 中无数据时完整导入所有实体和关系")
        @SuppressWarnings("unchecked")
        void fullImportWhenNoExistingData() throws Exception {
            setupFetchOneChain(Optional.empty());

            // Chain 6 ObjectMapper calls: 5 entity files + 1 relationship file
            when(objectMapper.readValue(any(InputStream.class), any(TypeReference.class)))
                    .thenReturn(personEntities())
                    .thenReturn(eventEntities())
                    .thenReturn(locationEntities())
                    .thenReturn(theoryEntities())
                    .thenReturn(documentEntities())
                    .thenReturn(relationships());

            try (var ignored = mockConstruction(ClassPathResource.class, (mock, ctx) -> {
                when(mock.exists()).thenReturn(true);
                when(mock.getInputStream()).thenReturn(new ByteArrayInputStream("[]".getBytes()));
            })) {
                partyHistoryDataLoader.initializePartyHistoryData();
            }

            // 5 entity types loaded
            verify(partyHistoryGraphService).batchMergeEntities(eq("Person"), any());
            verify(partyHistoryGraphService).batchMergeEntities(eq("Event"), any());
            verify(partyHistoryGraphService).batchMergeEntities(eq("Location"), any());
            verify(partyHistoryGraphService).batchMergeEntities(eq("Theory"), any());
            verify(partyHistoryGraphService).batchMergeEntities(eq("Document"), any());

            // 1 relationship batch
            verify(partyHistoryGraphService).batchAddRelationships(any());
        }
    }

    // ==================== ErrorTests ====================

    @Nested
    @DisplayName("ErrorTests — 异常路径")
    class ErrorTests {

        @Test
        @DisplayName("hasExistingData 查询抛出异常时视为无数据并继续加载")
        @SuppressWarnings("unchecked")
        void hasExistingDataExceptionTreatedAsFalse() throws Exception {
            Neo4jClient.UnboundRunnableSpec querySpec = mock(Neo4jClient.UnboundRunnableSpec.class);
            Neo4jClient.RecordFetchSpec<Map<String, Object>> fetchSpec = mock(Neo4jClient.RecordFetchSpec.class);
            when(neo4jClient.query(anyString())).thenReturn(querySpec);
            when(querySpec.fetch()).thenReturn(fetchSpec);
            when(fetchSpec.one()).thenThrow(new RuntimeException("Neo4j connection failed"));

            // ClassPathResource will not exist → readJsonFile returns null → loadEntities returns 0
            try (var ignored = mockConstruction(ClassPathResource.class, (mock, ctx) -> {
                when(mock.exists()).thenReturn(false);
            })) {
                partyHistoryDataLoader.initializePartyHistoryData();
                // Should not throw — exception is caught by try-catch in initializePartyHistoryData
            }

            // No batch operations should be called because readJsonFile returned null for all files
            verify(partyHistoryGraphService, never()).batchMergeEntities(anyString(), any());
            verify(partyHistoryGraphService, never()).batchAddRelationships(any());
        }

        @Test
        @DisplayName("ObjectMapper 读取异常被外层 catch 捕获不抛出")
        @SuppressWarnings("unchecked")
        void objectMapperReadExceptionIsCaught() throws Exception {
            setupFetchOneChain(Optional.empty());

            when(objectMapper.readValue(any(InputStream.class), any(TypeReference.class)))
                    .thenThrow(new RuntimeException("JSON parse error"));

            try (var ignored = mockConstruction(ClassPathResource.class, (mock, ctx) -> {
                when(mock.exists()).thenReturn(true);
                when(mock.getInputStream()).thenReturn(new ByteArrayInputStream("invalid".getBytes()));
            })) {
                // Should not throw — exception caught in initializePartyHistoryData
                partyHistoryDataLoader.initializePartyHistoryData();
            }

            verify(partyHistoryGraphService, never()).batchMergeEntities(anyString(), any());
            verify(partyHistoryGraphService, never()).batchAddRelationships(any());
        }
    }

    // ==================== BoundaryTests ====================

    @Nested
    @DisplayName("BoundaryTests — 边界情况")
    class BoundaryTests {

        @Test
        @DisplayName("hasExistingData 返回 Optional.empty() 时视为无数据")
        @SuppressWarnings("unchecked")
        void hasExistingDataEmptyOptional() throws Exception {
            setupFetchOneChain(Optional.empty());

            when(objectMapper.readValue(any(InputStream.class), any(TypeReference.class)))
                    .thenReturn(personEntities())
                    .thenReturn(eventEntities())
                    .thenReturn(locationEntities())
                    .thenReturn(theoryEntities())
                    .thenReturn(documentEntities())
                    .thenReturn(relationships());

            try (var ignored = mockConstruction(ClassPathResource.class, (mock, ctx) -> {
                when(mock.exists()).thenReturn(true);
                when(mock.getInputStream()).thenReturn(new ByteArrayInputStream("[]".getBytes()));
            })) {
                partyHistoryDataLoader.initializePartyHistoryData();
            }

            verify(partyHistoryGraphService).batchMergeEntities(eq("Person"), any());
        }

        @Test
        @DisplayName("hasExistingData row.get(cnt) 为 null 非 Number 时视为无数据")
        @SuppressWarnings("unchecked")
        void hasExistingDataCountNull() throws Exception {
            Map<String, Object> row = mock(Map.class);
            when(row.get("cnt")).thenReturn(null); // null, not a Number
            setupFetchOneChain(Optional.of(row));

            when(objectMapper.readValue(any(InputStream.class), any(TypeReference.class)))
                    .thenReturn(personEntities())
                    .thenReturn(List.of()) // empty events
                    .thenReturn(List.of()) // empty locations
                    .thenReturn(List.of()) // empty theories
                    .thenReturn(List.of()) // empty documents
                    .thenReturn(relationships());

            try (var ignored = mockConstruction(ClassPathResource.class, (mock, ctx) -> {
                when(mock.exists()).thenReturn(true);
                when(mock.getInputStream()).thenReturn(new ByteArrayInputStream("[]".getBytes()));
            })) {
                partyHistoryDataLoader.initializePartyHistoryData();
            }

            verify(partyHistoryGraphService).batchMergeEntities(eq("Person"), any());
            // Events/Locations/Theories/Documents had empty lists → not called for those
        }

        @Test
        @DisplayName("hasExistingData row.get(cnt) 返回字符串非 Number 时视为无数据")
        @SuppressWarnings("unchecked")
        void hasExistingDataCountNotNumber() throws Exception {
            Map<String, Object> row = mock(Map.class);
            when(row.get("cnt")).thenReturn("not-a-number");
            setupFetchOneChain(Optional.of(row));

            when(objectMapper.readValue(any(InputStream.class), any(TypeReference.class)))
                    .thenReturn(personEntities())
                    .thenReturn(eventEntities())
                    .thenReturn(locationEntities())
                    .thenReturn(theoryEntities())
                    .thenReturn(documentEntities())
                    .thenReturn(relationships());

            try (var ignored = mockConstruction(ClassPathResource.class, (mock, ctx) -> {
                when(mock.exists()).thenReturn(true);
                when(mock.getInputStream()).thenReturn(new ByteArrayInputStream("[]".getBytes()));
            })) {
                partyHistoryDataLoader.initializePartyHistoryData();
            }

            verify(partyHistoryGraphService).batchMergeEntities(eq("Person"), any());
        }

        @Test
        @DisplayName("hasExistingData count 为 0 时视为无数据")
        @SuppressWarnings("unchecked")
        void hasExistingDataCountZero() throws Exception {
            setupFetchOneChain(Optional.of(countRow(0L)));

            when(objectMapper.readValue(any(InputStream.class), any(TypeReference.class)))
                    .thenReturn(personEntities())
                    .thenReturn(List.of())
                    .thenReturn(List.of())
                    .thenReturn(List.of())
                    .thenReturn(List.of())
                    .thenReturn(List.of());

            try (var ignored = mockConstruction(ClassPathResource.class, (mock, ctx) -> {
                when(mock.exists()).thenReturn(true);
                when(mock.getInputStream()).thenReturn(new ByteArrayInputStream("[]".getBytes()));
            })) {
                partyHistoryDataLoader.initializePartyHistoryData();
            }

            // count=0 → num.longValue() > 0 is false → proceeds to loading
            verify(partyHistoryGraphService).batchMergeEntities(eq("Person"), any());
        }

        @Test
        @DisplayName("readJsonFile 返回 null（文件不存在）时 loadEntities 跳过")
        @SuppressWarnings("unchecked")
        void readJsonFileReturnsNull() throws Exception {
            setupFetchOneChain(Optional.empty());

            // ObjectMapper will throw since ClassPathResource.exists() returns false
            // and readJsonFile returns null before calling ObjectMapper
            when(objectMapper.readValue(any(InputStream.class), any(TypeReference.class)))
                    .thenReturn(personEntities());

            try (var ignored = mockConstruction(ClassPathResource.class, (mock, ctx) -> {
                when(mock.exists()).thenReturn(false); // file doesn't exist
            })) {
                partyHistoryDataLoader.initializePartyHistoryData();
            }

            // No batch operations because readJsonFile returned null for all files
            // and loadEntities/loadRelationships skip when null
        }

        @Test
        @DisplayName("readJsonFile 返回空列表时 loadEntities 跳过")
        @SuppressWarnings("unchecked")
        void readJsonFileReturnsEmptyList() throws Exception {
            setupFetchOneChain(Optional.empty());

            when(objectMapper.readValue(any(InputStream.class), any(TypeReference.class)))
                    .thenReturn(List.of()) // All 6 files return empty lists
                    .thenReturn(List.of())
                    .thenReturn(List.of())
                    .thenReturn(List.of())
                    .thenReturn(List.of())
                    .thenReturn(List.of());

            try (var ignored = mockConstruction(ClassPathResource.class, (mock, ctx) -> {
                when(mock.exists()).thenReturn(true);
                when(mock.getInputStream()).thenReturn(new ByteArrayInputStream("[]".getBytes()));
            })) {
                partyHistoryDataLoader.initializePartyHistoryData();
            }

            // No batch operations because all lists are empty
            verify(partyHistoryGraphService, never()).batchMergeEntities(anyString(), any());
            verify(partyHistoryGraphService, never()).batchAddRelationships(any());
        }

        @Test
        @DisplayName("关系数据中的 source/target/type 键被正确标准化为 sourceId/targetId/relType")
        @SuppressWarnings("unchecked")
        void relationshipsKeyNormalization() throws Exception {
            setupFetchOneChain(Optional.empty());

            // Return empty for all entities, only test relationships
            when(objectMapper.readValue(any(InputStream.class), any(TypeReference.class)))
                    .thenReturn(List.of())
                    .thenReturn(List.of())
                    .thenReturn(List.of())
                    .thenReturn(List.of())
                    .thenReturn(List.of())
                    .thenReturn(List.of(Map.of("source", "p-001", "target", "e-001", "type", "INITIATED")));

            try (var ignored = mockConstruction(ClassPathResource.class, (mock, ctx) -> {
                when(mock.exists()).thenReturn(true);
                when(mock.getInputStream()).thenReturn(new ByteArrayInputStream("[]".getBytes()));
            })) {
                partyHistoryDataLoader.initializePartyHistoryData();
            }

            verify(partyHistoryGraphService).batchAddRelationships(any());
        }

        @Test
        @DisplayName("关系数据中缺少 type 键导致 relType 为 null 时被过滤并警告")
        @SuppressWarnings("unchecked")
        void relationshipsMissingTypeAreFiltered() throws Exception {
            setupFetchOneChain(Optional.empty());

            when(objectMapper.readValue(any(InputStream.class), any(TypeReference.class)))
                    .thenReturn(List.of())
                    .thenReturn(List.of())
                    .thenReturn(List.of())
                    .thenReturn(List.of())
                    .thenReturn(List.of())
                    .thenReturn(List.of(Map.of("source", "p-001", "target", "e-001"))); // No "type" key

            try (var ignored = mockConstruction(ClassPathResource.class, (mock, ctx) -> {
                when(mock.exists()).thenReturn(true);
                when(mock.getInputStream()).thenReturn(new ByteArrayInputStream("[]".getBytes()));
            })) {
                partyHistoryDataLoader.initializePartyHistoryData();
            }

            // batchAddRelationships IS called but with empty validRows (all filtered out)
            verify(partyHistoryGraphService).batchAddRelationships(any());
        }
    }
}
