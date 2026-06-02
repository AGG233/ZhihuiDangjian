package com.rauio.smartdangjian.server.graph.controller.admin;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.security.RoleConstants;
import com.rauio.smartdangjian.server.graph.pojo.request.PartyHistoryEntityImportRequest;
import com.rauio.smartdangjian.server.graph.pojo.request.PartyHistoryRelationshipImportRequest;
import com.rauio.smartdangjian.server.graph.service.PartyHistoryGraphService;
import com.rauio.smartdangjian.server.graph.service.PartyHistoryImportService;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "党史知识图谱管理", description = "党史实体与关系的批量导入和管理")
@RestController
@RequestMapping("/api/graph/party-history/admin")
@RequiredArgsConstructor
@SaCheckRole(RoleConstants.MANAGER)
@Validated
public class AdminPartyHistoryController {

    private final PartyHistoryImportService importService;
    private final PartyHistoryGraphService graphService;

    @Operation(summary = "批量导入实体", description = "按标签批量导入党史实体节点")
    @PostMapping("/import/entities/{label}")
    public Result<Integer> importEntities(
            @Parameter(description = "节点标签") @PathVariable String label,
            @RequestBody @Valid @NotEmpty(message = "实体列表不能为空") @Size(max = 500, message = "单次最多导入500个实体")
                    List<@Valid PartyHistoryEntityImportRequest> entities) {
        return Result.ok(importService.importEntities(
                label,
                entities.stream().map(PartyHistoryEntityImportRequest::toMap).toList()));
    }

    @Operation(summary = "批量导入关系", description = "批量导入党史实体间的关系")
    @PostMapping("/import/relationships")
    public Result<Integer> importRelationships(
            @RequestBody @Valid @NotEmpty(message = "关系列表不能为空") @Size(max = 500, message = "单次最多导入500条关系")
                    List<@Valid PartyHistoryRelationshipImportRequest> relationships) {
        return Result.ok(importService.importRelationships(relationships.stream()
                .map(PartyHistoryRelationshipImportRequest::toMap)
                .toList()));
    }

    @Operation(summary = "删除实体", description = "根据 graph_id 删除党史实体及其所有关联关系")
    @DeleteMapping("/entities/{graphId}")
    public Result<Void> deleteEntity(@Parameter(description = "实体 graph_id") @PathVariable String graphId) {
        graphService.deleteEntity(graphId);
        return Result.ok();
    }
}
