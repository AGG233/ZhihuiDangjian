package com.rauio.smartdangjian.pojo.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GraphEdgeVO {
    private String source;
    private String target;
    private String type;
}
