package com.rauio.smartdangjian.server.ai.agent;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;

import java.util.EnumMap;
import java.util.Map;

public class AiAgentRegistry {

    private final Map<AiAgentType, ReactAgent> agentMap;

    public AiAgentRegistry(Map<AiAgentType, ReactAgent> agentMap) {
        this.agentMap = new EnumMap<>(agentMap);
    }

    public ReactAgent get(AiAgentType type) {
        ReactAgent agent = agentMap.get(type);
        if (agent == null) {
            throw new IllegalArgumentException("未注册的Agent类型: " + type);
        }
        return agent;
    }
}
