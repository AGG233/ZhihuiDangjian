package com.rauio.smartdangjian.server.graph.pojo.request;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PartyHistoryRelationshipImportRequest {

    @NotBlank(message = "source不能为空")
    private String source;

    @NotBlank(message = "target不能为空")
    private String target;

    @NotBlank(message = "type不能为空")
    @Pattern(regexp = "[A-Z_][A-Z0-9_]*", message = "type只能包含大写字母、数字和下划线，且不能以数字开头")
    private String type;

    private Map<String, Object> properties = new LinkedHashMap<>();

    public Map<String, Object> toMap() {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("source", source);
        row.put("target", target);
        row.put("type", type);
        row.put("properties", properties == null ? Map.of() : new LinkedHashMap<>(properties));
        return row;
    }
}
