package com.rauio.smartdangjian.constants;

/**
 * Redis相关常量定义
 */
public class RedisConstants {
    // Redis中各种键的前缀
    public static final String USER_CACHE_PREFIX = "user:info:";
    public static final String COURSE_CACHE_PREFIX = "course:info:";
    public static final String CHAPTER_CACHE_PREFIX = "chapter:info:";

    public static final String AI_PROMPT_HASH_KEY = "ai:prompt:items";
    public static final String AI_PROMPT_SEQ_KEY = "ai:prompt:seq";
}
