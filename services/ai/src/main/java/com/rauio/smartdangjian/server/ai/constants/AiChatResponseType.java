package com.rauio.smartdangjian.server.ai.constants;

/**
 * AI 聊天流式响应事件类型常量
 */
public final class AiChatResponseType {

    private AiChatResponseType() {
        // 禁止实例化
    }

    /** 流开始 */
    public static final String START = "START";

    /** AI 思考内容 */
    public static final String THINKING = "THINKING";

    /** AI 文本输出（流式片段） */
    public static final String TEXT = "TEXT";

    /** AI 输出完成 */
    public static final String FINISHED = "FINISHED";

    /** 工具调用 */
    public static final String TOOL_CALL = "TOOL_CALL";

    /** 工具结果 */
    public static final String TOOL_RESULT = "TOOL_RESULT";

    /** 错误 */
    public static final String ERROR = "ERROR";

    /** 流结束 */
    public static final String END = "END";

    /** 其他 */
    public static final String OTHER = "OTHER";
}
