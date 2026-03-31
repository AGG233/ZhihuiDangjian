package com.rauio.smartdangjian.server.ai.agent;

public enum AiAgentType {
    CHAT("CHAT", "chat-agent", "处理党务学习场景下的日常问答"),
    QUIZ("QUIZ", "quiz-agent", "根据主题和学习记录生成测试小题"),
    EVALUATION("EVALUATION", "evaluation-agent", "结合学习记录和答题情况给出学习评估");

    private final String code;
    private final String agentName;
    private final String description;

    AiAgentType(String code, String agentName, String description) {
        this.code = code;
        this.agentName = agentName;
        this.description = description;
    }

    public String code() {
        return code;
    }

    public String agentName() {
        return agentName;
    }

    public String description() {
        return description;
    }
}
