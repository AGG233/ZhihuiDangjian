package com.rauio.smartdangjian.server.graph.pojo.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 党史种子数据导入结果。
 */
@Data
@Builder
@Schema(description = "党史种子数据导入结果")
public class PartySeedImportResponse {

    @Schema(description = "导入人物节点数")
    private int personCount;

    @Schema(description = "导入事件节点数")
    private int eventCount;

    @Schema(description = "导入理论节点数")
    private int theoryCount;

    @Schema(description = "导入人物-事件关系数")
    private int personEventCount;

    @Schema(description = "导入文献-理论关系数")
    private int documentTheoryCount;

    @Schema(description = "导入总行数")
    private int total;
}
