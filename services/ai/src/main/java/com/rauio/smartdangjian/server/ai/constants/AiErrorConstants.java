package com.rauio.smartdangjian.server.ai.constants;

/**
 * AI 模块错误码常量（范围 8000-8999）
 */
public class AiErrorConstants {

    public static final int AGENT_NOT_REGISTERED = 8001;
    public static final int SKILL_NOT_FOUND = 8002;
    public static final int SKILL_NOT_IN_CACHE = 8003;
    public static final int PROMPT_NOT_FOUND = 8004;
    // 8005 曾为 VOICE_TRANSCRIBE_FAILED（语音转写失败），接口下线后保留空号不复用
    public static final int DOCUMENT_TYPE_INVALID = 8006;
    public static final int DOCUMENT_INGEST_FAILED = 8007;
    public static final int USER_ID_REQUIRED = 8008;
}
