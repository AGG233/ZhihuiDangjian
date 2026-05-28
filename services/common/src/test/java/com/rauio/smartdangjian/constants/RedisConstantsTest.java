package com.rauio.smartdangjian.constants;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RedisConstantsTest {

    @Test
    @DisplayName("USER_VO_CACHE_PREFIX 为 user:data:")
    void userVoCachePrefix() {
        assertThat(RedisConstants.USER_VO_CACHE_PREFIX).isEqualTo("user:data:");
    }

    @Test
    @DisplayName("USER_CACHE_PREFIX 为 user:entity:")
    void userCachePrefix() {
        assertThat(RedisConstants.USER_CACHE_PREFIX).isEqualTo("user:entity:");
    }

    @Test
    @DisplayName("COURSE_CACHE_PREFIX 为 course:info:")
    void courseCachePrefix() {
        assertThat(RedisConstants.COURSE_CACHE_PREFIX).isEqualTo("course:info:");
    }

    @Test
    @DisplayName("CHAPTER_CACHE_PREFIX 为 chapter:info:")
    void chapterCachePrefix() {
        assertThat(RedisConstants.CHAPTER_CACHE_PREFIX).isEqualTo("chapter:info:");
    }

    @Test
    @DisplayName("AI_PROMPT_HASH_KEY 为 ai:prompt:items")
    void aiPromptHashKey() {
        assertThat(RedisConstants.AI_PROMPT_HASH_KEY).isEqualTo("ai:prompt:items");
    }

    @Test
    @DisplayName("AI_PROMPT_SEQ_KEY 为 ai:prompt:seq")
    void aiPromptSeqKey() {
        assertThat(RedisConstants.AI_PROMPT_SEQ_KEY).isEqualTo("ai:prompt:seq");
    }

    @Test
    @DisplayName("USER_PROFILE_CACHE_PREFIX 为 user:profile:")
    void userProfileCachePrefix() {
        assertThat(RedisConstants.USER_PROFILE_CACHE_PREFIX).isEqualTo("user:profile:");
    }
}
