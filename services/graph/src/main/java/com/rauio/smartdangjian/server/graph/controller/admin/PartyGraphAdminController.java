package com.rauio.smartdangjian.server.graph.controller.admin;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rauio.smartdangjian.pojo.response.Result;
import com.rauio.smartdangjian.server.graph.pojo.response.PartySeedImportResponse;
import com.rauio.smartdangjian.server.graph.service.PartySeedDataImporter;

import cn.dev33.satoken.annotation.SaCheckRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "党史图谱管理接口", description = "基于Neo4j的党史实体图谱管理接口")
@RestController
@RequestMapping("/api/graph/admin/party/seed")
@RequiredArgsConstructor
public class PartyGraphAdminController {

    private final PartySeedDataImporter partySeedDataImporter;

    @Operation(summary = "导入党史种子数据", description = "读取种子 CSV 并幂等 MERGE 导入党史节点与关系，返回各类导入计数")
    @PostMapping("/import")
    @SaCheckRole("MANAGER")
    public Result<PartySeedImportResponse> importSeed() {
        return Result.ok(partySeedDataImporter.importAll());
    }
}
