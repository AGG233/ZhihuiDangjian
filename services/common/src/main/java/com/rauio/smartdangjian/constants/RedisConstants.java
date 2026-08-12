package com.rauio.smartdangjian.constants;

/**
 * Redis相关常量定义
 */
public class RedisConstants {
    // Redis中各种键的前缀
    public static final String USER_VO_CACHE_PREFIX = "user:data:";
    public static final String USER_CACHE_PREFIX = "user:entity:";
    public static final String COURSE_CACHE_PREFIX = "course:info:";
    public static final String CHAPTER_CACHE_PREFIX = "chapter:info:";

    public static final String AI_PROMPT_HASH_KEY = "ai:prompt:items";
    public static final String AI_PROMPT_SEQ_KEY = "ai:prompt:seq";

    public static final String USER_PROFILE_CACHE_PREFIX = "user:profile:";

    public static final String HOT_COURSE_CACHE_PREFIX = "search:hot:courses:";
    public static final String HOT_CATEGORY_CACHE_PREFIX = "search:hot:categories:";
    public static final String LEARNING_TREND_CACHE_PREFIX = "search:trend:learning:";
}
