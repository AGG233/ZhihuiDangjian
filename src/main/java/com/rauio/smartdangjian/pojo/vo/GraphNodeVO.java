package com.rauio.smartdangjian.pojo.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GraphNodeVO {
    private String id;
    private String label;
    private String name;
}
