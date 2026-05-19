package com.rauio.smartdangjian.server.ai.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.EnumMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.agent.flow.agent.LlmRoutingAgent;
import com.rauio.smartdangjian.exception.BusinessException;

@ExtendWith(MockitoExtension.class)
class AiAgentRegistryTest {

    @Mock
    private LlmRoutingAgent coordinator;

    @Mock
    private ReactAgent studyAssistant;

    @Mock
    private ReactAgent contentDiscovery;

    private AiAgentRegistry registry;

    @BeforeEach
    void setUp() {
        Map<AiAgentType, ReactAgent> map = new EnumMap<>(AiAgentType.class);
        map.put(AiAgentType.STUDY_ASSISTANT, studyAssistant);
        map.put(AiAgentType.CONTENT_DISCOVERY, contentDiscovery);
        registry = new AiAgentRegistry(coordinator, map);
    }

    @Test
    @DisplayName("getCoordinator 返回构造时传入的协调器")
    void getCoordinator() {
        assertThat(registry.getCoordinator()).isSameAs(coordinator);
    }

    @Test
    @DisplayName("getSpecialist 返回已注册的 Agent 实例")
    void getSpecialistReturnsRegistered() {
        assertThat(registry.getSpecialist(AiAgentType.STUDY_ASSISTANT)).isSameAs(studyAssistant);
        assertThat(registry.getSpecialist(AiAgentType.CONTENT_DISCOVERY)).isSameAs(contentDiscovery);
    }

    @Test
    @DisplayName("getSpecialist 未注册类型抛出 BusinessException")
    void getSpecialistThrowsForUnregistered() {
        assertThatThrownBy(() -> registry.getSpecialist(AiAgentType.ASSESSMENT))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("未注册的Agent类型");
    }
}
