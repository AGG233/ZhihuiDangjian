package com.rauio.smartdangjian.server.ai.rag;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rauio.smartdangjian.pojo.response.Result;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

/**
 * RAG 文档入库接口（管理端）。
 */
@Tag(name = "RAG文档入库接口", description = "将文章/章节内容向量化入库到 Neo4j，供 RAG 检索使用")
@RestController
@RequestMapping("/api/ai/rag/documents")
@RequiredArgsConstructor
@SaCheckRole("MANAGER")
public class DocumentIngestionController {

    private final DocumentIngestionService documentIngestionService;

    @Operation(summary = "全量入库", description = "遍历全部文章与章节，切分后向量化写入向量库")
    @PostMapping("/ingest")
    public Result<Integer> ingestAll() {
        return Result.ok(documentIngestionService.ingestAll());
    }

    @Operation(summary = "增量入库", description = "按类型与 ID 入库单条文章或章节")
    @PostMapping("/ingest/{type}/{id}")
    public Result<Integer> ingestById(@PathVariable String type, @PathVariable String id) {
        return Result.ok(documentIngestionService.ingestById(type, id));
    }
}
