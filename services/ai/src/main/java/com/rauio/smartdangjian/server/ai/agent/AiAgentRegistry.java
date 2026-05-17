package com.rauio.smartdangjian.server.ai.agent;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;

import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.ai.constants.AiErrorConstants;

import java.util.EnumMap;
import java.util.Map;

public class AiAgentRegistry {

    private final LlmRoutingAgent coordinator;
    private final Map<AiAgentType, ReactAgent> specialistMap;

    public AiAgentRegistry(LlmRoutingAgent coordinator, Map<AiAgentType, ReactAgent> specialistMap) {
        this.coordinator = coordinator;
        this.specialistMap = new EnumMap<>(specialistMap);
    }

    public LlmRoutingAgent getCoordinator() {
        return coordinator;
    }

    public ReactAgent getSpecialist(AiAgentType type) {
        ReactAgent agent = specialistMap.get(type);
        if (agent == null) {
            throw new BusinessException(AiErrorConstants.AGENT_NOT_REGISTERED, "未注册的Agent类型: " + type);
        }
        return agent;
    }
}
