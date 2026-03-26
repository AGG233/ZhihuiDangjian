package com.rauio.smartdangjian.server.ai.service;

import com.rauio.smartdangjian.server.ai.pojo.entity.AiSystemPrompt;
import com.rauio.smartdangjian.server.ai.pojo.request.AiPromptCreateRequest;
import com.rauio.smartdangjian.server.ai.pojo.request.AiPromptUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import static com.rauio.smartdangjian.constants.AiPromptConstants.COMMON_SYSTEM_PROMPTS;
import static com.rauio.smartdangjian.constants.AiPromptConstants.EVALUATION_SYSTEM_PROMPTS;
import static com.rauio.smartdangjian.constants.AiPromptConstants.QUIZ_SYSTEM_PROMPTS;
import static com.rauio.smartdangjian.constants.RedisConstants.AI_PROMPT_HASH_KEY;
import static com.rauio.smartdangjian.constants.RedisConstants.AI_PROMPT_SEQ_KEY;

@Service
@RequiredArgsConstructor
public class PromptService {

    public static final String PROMPT_TYPE_COMMON = "COMMON";
    public static final String PROMPT_TYPE_EVALUATION = "EVALUATION";
    public static final String PROMPT_TYPE_QUIZ = "QUIZ";

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 创建系统提示词。
     *
     * @param request 提示词创建参数
     * @return 已创建的提示词
     */
    public AiSystemPrompt create(AiPromptCreateRequest request) {
        String type = normalizeType(request.getType());
        String content = normalizeContent(request.getContent());
        Boolean enabled = request.getEnabled() == null ? Boolean.TRUE : request.getEnabled();
        Integer sort = request.getSort() == null ? 0 : request.getSort();

        String id = String.valueOf(redisTemplate.opsForValue().increment(AI_PROMPT_SEQ_KEY));
        AiSystemPrompt prompt = AiSystemPrompt.builder()
                .id(id)
                .type(type)
                .content(content)
                .enabled(enabled)
                .sort(sort)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        redisTemplate.opsForHash().put(AI_PROMPT_HASH_KEY, id, prompt);
        return prompt;
    }

    /**
     * 根据 ID 获取提示词。
     *
     * @param id 提示词 ID
     * @return 提示词对象
     */
    public AiSystemPrompt get(String id) {
        Object value = redisTemplate.opsForHash().get(AI_PROMPT_HASH_KEY, id);
        if (value instanceof AiSystemPrompt) {
            return (AiSystemPrompt) value;
        }
        return null;
    }

    /**
     * 按类型和启用状态查询提示词列表。
     *
     * @param type 提示词类型
     * @param enabled 是否启用
     * @return 提示词列表
     */
    public List<AiSystemPrompt> list(String type, Boolean enabled) {
        String normalizedType = StringUtils.hasText(type) ? normalizeType(type) : null;
        List<AiSystemPrompt> prompts = getAllPrompts();
        return prompts.stream()
                .filter(prompt -> normalizedType == null || normalizedType.equals(prompt.getType()))
                .filter(prompt -> enabled == null || enabled.equals(prompt.getEnabled()))
                .sorted(promptComparator())
                .collect(Collectors.toList());
    }

    /**
     * 更新提示词配置。
     *
     * @param id 提示词 ID
     * @param request 提示词更新参数
     * @return 是否更新成功
     */
    public boolean update(String id, AiPromptUpdateRequest request) {
        AiSystemPrompt existing = get(id);
        if (existing == null) {
            return false;
        }
        if (StringUtils.hasText(request.getType())) {
            existing.setType(normalizeType(request.getType()));
        }
        if (request.getContent() != null) {
            existing.setContent(normalizeContent(request.getContent()));
        }
        if (request.getEnabled() != null) {
            existing.setEnabled(request.getEnabled());
        }
        if (request.getSort() != null) {
            existing.setSort(request.getSort());
        }
        existing.setUpdatedAt(LocalDateTime.now());
        redisTemplate.opsForHash().put(AI_PROMPT_HASH_KEY, id, existing);
        return true;
    }

    /**
     * 删除提示词。
     *
     * @param id 提示词 ID
     * @return 是否删除成功
     */
    public boolean delete(String id) {
        Long removed = redisTemplate.opsForHash().delete(AI_PROMPT_HASH_KEY, id);
        return removed > 0;
    }

    /**
     * 获取指定类型的有效提示词集合。
     *
     * @param type 提示词类型
     * @return 提示词内容列表
     */
    public List<String> getPrompts(String type) {
        String normalizedType = normalizeType(type);
        if (PROMPT_TYPE_EVALUATION.equals(normalizedType)) {
            return mergePrompts(PROMPT_TYPE_COMMON, PROMPT_TYPE_EVALUATION);
        }
        if (PROMPT_TYPE_QUIZ.equals(normalizedType)) {
            return mergePrompts(PROMPT_TYPE_COMMON, PROMPT_TYPE_QUIZ);
        }
        return getOrSeedPrompts(normalizedType);
    }

    /**
     * 查询指定类型已启用的提示词内容。
     *
     * @param type 提示词类型
     * @return 提示词内容列表
     */
    private List<String> listPromptsByType(String type) {
        return list(type, true).stream()
                .map(AiSystemPrompt::getContent)
                .collect(Collectors.toList());
    }

    /**
     * 获取 Redis 中全部提示词。
     *
     * @return 提示词列表
     */
    private List<AiSystemPrompt> getAllPrompts() {
        Collection<Object> values = redisTemplate.opsForHash().values(AI_PROMPT_HASH_KEY);
        List<AiSystemPrompt> prompts = new ArrayList<>();
        for (Object value : values) {
            if (value instanceof AiSystemPrompt) {
                prompts.add((AiSystemPrompt) value);
            }
        }
        return prompts;
    }

    /**
     * 构造提示词排序规则。
     *
     * @return 提示词比较器
     */
    private Comparator<AiSystemPrompt> promptComparator() {
        return Comparator
                .comparing(AiSystemPrompt::getSort, Comparator.nullsLast(Integer::compareTo))
                .thenComparing(AiSystemPrompt::getCreatedAt, Comparator.nullsLast(LocalDateTime::compareTo))
                .thenComparing(AiSystemPrompt::getId, Comparator.nullsLast(String::compareTo));
    }

    /**
     * 获取指定类型的默认提示词。
     *
     * @param type 提示词类型
     * @return 默认提示词列表
     */
    private List<String> defaultPromptsByType(String type) {
        if (PROMPT_TYPE_COMMON.equals(type)) {
            return COMMON_SYSTEM_PROMPTS;
        }
        if (PROMPT_TYPE_EVALUATION.equals(type)) {
            return EVALUATION_SYSTEM_PROMPTS;
        }
        if (PROMPT_TYPE_QUIZ.equals(type)) {
            return QUIZ_SYSTEM_PROMPTS;
        }
        return List.of();
    }

    /**
     * 获取提示词；缺失时用默认值初始化。
     *
     * @param type 提示词类型
     * @return 提示词内容列表
     */
    private List<String> getOrSeedPrompts(String type) {
        List<String> prompts = listPromptsByType(type);
        if (!prompts.isEmpty()) {
            return prompts;
        }
        List<String> defaults = defaultPromptsByType(type);
        for (String content : defaults) {
            createPromptInternal(type, content);
        }
        return defaults;
    }

    /**
     * 合并两类提示词。
     *
     * @param primaryType 主提示词类型
     * @param secondaryType 次提示词类型
     * @return 合并后的提示词列表
     */
    private List<String> mergePrompts(String primaryType, String secondaryType) {
        List<String> primary = getOrSeedPrompts(primaryType);
        List<String> secondary = getOrSeedPrompts(secondaryType);
        List<String> merged = new ArrayList<>(primary.size() + secondary.size());
        merged.addAll(primary);
        merged.addAll(secondary);
        return merged;
    }

    /**
     * 创建内部默认提示词。
     *
     * @param type 提示词类型
     * @param content 提示词内容
     */
    private void createPromptInternal(String type, String content) {
        String id = String.valueOf(redisTemplate.opsForValue().increment(AI_PROMPT_SEQ_KEY));
        AiSystemPrompt prompt = AiSystemPrompt.builder()
                .id(id)
                .type(type)
                .content(content)
                .enabled(true)
                .sort(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        redisTemplate.opsForHash().put(AI_PROMPT_HASH_KEY, id, prompt);
    }

    /**
     * 规范化提示词类型。
     *
     * @param type 原始提示词类型
     * @return 标准化后的提示词类型
     */
    private String normalizeType(String type) {
        String normalized = type.trim().toUpperCase();
        if (!PROMPT_TYPE_COMMON.equals(normalized)
                && !PROMPT_TYPE_EVALUATION.equals(normalized)
                && !PROMPT_TYPE_QUIZ.equals(normalized)) {
            throw new IllegalArgumentException("type只支持COMMON、EVALUATION或QUIZ");
        }
        return normalized;
    }

    /**
     * 规范化提示词内容。
     *
     * @param content 原始提示词内容
     * @return 标准化后的提示词内容
     */
    private String normalizeContent(String content) {
        return content.trim();
    }
}
