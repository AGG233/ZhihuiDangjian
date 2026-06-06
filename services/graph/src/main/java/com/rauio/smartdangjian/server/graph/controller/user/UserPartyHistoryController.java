package com.rauio.smartdangjian.server.graph.controller.user;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.security.RoleConstants;
import com.rauio.smartdangjian.server.graph.pojo.response.KnowledgeGraphResponse;
import com.rauio.smartdangjian.server.graph.service.PartyHistoryQueryService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "党史知识图谱接口", description = "党史人物、事件、理论等知识图谱查询")
@RestController
@RequestMapping("/api/graph/party-history")
@RequiredArgsConstructor
@SaCheckRole(RoleConstants.STUDENT)
public class UserPartyHistoryController {

    private final PartyHistoryQueryService queryService;

    @Operation(summary = "搜索党史实体", description = "按关键词搜索党史人物、事件、地点、理论、文献")
    @GetMapping("/search")
    public Result<KnowledgeGraphResponse> search(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "实体类型过滤") @RequestParam(required = false) List<String> entityTypes,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") int size) {
        return Result.ok(queryService.searchEntities(keyword, entityTypes, page, size));
    }

    @Operation(summary = "获取实体详情", description = "获取指定党史实体及其直接关联的节点和关系")
    @GetMapping("/entities/{graphId}")
    public Result<KnowledgeGraphResponse> getDetail(
            @Parameter(description = "实体 graph_id") @PathVariable String graphId) {
        return Result.ok(queryService.getEntityDetail(graphId));
    }

    @Operation(summary = "获取人物关联事件", description = "获取党史人物发起或参与的事件列表")
    @GetMapping("/persons/{graphId}/events")
    public Result<KnowledgeGraphResponse> getPersonEvents(
            @Parameter(description = "人物 graph_id") @PathVariable String graphId) {
        return Result.ok(queryService.getPersonEvents(graphId));
    }

    @Operation(summary = "获取事件时间线", description = "从事件出发沿因果关系展开多层时间线")
    @GetMapping("/events/{graphId}/timeline")
    public Result<KnowledgeGraphResponse> getEventTimeline(
            @Parameter(description = "事件 graph_id") @PathVariable String graphId,
            @Parameter(description = "展开深度") @RequestParam(defaultValue = "2") int depth) {
        return Result.ok(queryService.getEventTimeline(graphId, depth));
    }

    @Operation(summary = "获取理论演进", description = "获取理论沿 EVOLVED_FROM/DEVELOPED_INTO 关系的演进图谱")
    @GetMapping("/theories/{graphId}/evolution")
    public Result<KnowledgeGraphResponse> getTheoryEvolution(
            @Parameter(description = "理论 graph_id") @PathVariable String graphId) {
        return Result.ok(queryService.getTheoryEvolution(graphId));
    }
}
