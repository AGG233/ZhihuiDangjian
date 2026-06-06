package com.rauio.smartdangjian.server.graph.pojo.request;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.validation.constraints.AssertTrue;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;

public class PartyHistoryEntityImportRequest {

    private final Map<String, Object> properties = new LinkedHashMap<>();

    @JsonAnySetter
    public void put(String key, Object value) {
        properties.put(key, value);
    }

    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        return properties;
    }

    @AssertTrue(message = "实体属性不能为空")
    public boolean hasProperties() {
        return !properties.isEmpty();
    }

    public Map<String, Object> toMap() {
        return new LinkedHashMap<>(properties);
    }
}
