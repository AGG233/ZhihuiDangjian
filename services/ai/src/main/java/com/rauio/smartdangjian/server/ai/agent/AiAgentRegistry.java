package com.rauio.smartdangjian.server.ai.agent;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.ai.constants.AiErrorConstants;

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
            throw new BusinessException(AiErrorConstants.AGENT_NOT_REGISTERED, "未注册的Agent类型: " + type);
        }
        return agent;
    }
}
