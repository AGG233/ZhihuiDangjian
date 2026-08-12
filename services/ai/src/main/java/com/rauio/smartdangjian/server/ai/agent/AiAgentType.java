package com.rauio.smartdangjian.server.ai.agent;

public enum AiAgentType {
    STUDY_ASSISTANT("STUDY_ASSISTANT", "study-assistant-agent", "党务学习日常问答、知识讲解、学习指导"),
    CONTENT_DISCOVERY("CONTENT_DISCOVERY", "content-discovery-agent", "课程/文章/章节搜索、内容浏览、推荐"),
    ASSESSMENT("ASSESSMENT", "assessment-agent", "测试题目生成、答题评估、学习评估"),
    REVIEW("REVIEW", "review-agent", "内容质量审查与用户内容安全审核"),
    PROFILE("PROFILE", "profile-agent", "学习画像分析、学习路径规划、数据洞察");

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

    public static AiAgentType fromLegacyCode(String code) {
        return switch (code) {
            case "CHAT" -> STUDY_ASSISTANT;
            case "QUIZ", "EVALUATION" -> ASSESSMENT;
            default -> valueOf(code);
        };
    }
}
