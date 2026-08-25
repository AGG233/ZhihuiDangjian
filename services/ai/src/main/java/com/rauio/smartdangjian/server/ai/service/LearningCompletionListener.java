package com.rauio.smartdangjian.server.ai.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.rauio.smartdangjian.event.LearningCompletedEvent;
import com.rauio.smartdangjian.server.ai.pojo.request.AiChatRequest;
import com.rauio.smartdangjian.server.ai.tool.AiQuizGeneratorTool;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 学习完成事件监听器：学员学习完毕后自动化动作（自动出题、自动评估）的落地点。
 *
 * <p>章节首次完成时异步执行两项动作，均不阻塞学习主链路、失败互不影响：
 * <ul>
 *   <li>自动生成辅助题库：复用 {@link AiQuizGeneratorTool} 按章节内容批量出题并落库，
 *       同一用户同一章节 24 小时内去抖；全部题目生成失败时回滚去抖键以便重试；</li>
 *   <li>自动生成本次学习评估：经 assessment 路由的 Agent 生成评估报告，
 *       会话随 {@code AiMemoryService#saveConversation} 自动存档供用户回看，
 *       同样按 24 小时去抖（并发双发事件只评估一次），失败即回滚去抖键。</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LearningCompletionListener {

    /** 出题去抖键前缀（24 小时内同用户同章节只出一次） */
    static final String QUIZ_DEBOUNCE_KEY_PREFIX = "ai:quiz:done:";

    /** 评估去抖键前缀（与出题去抖相互独立，防并发双发事件重复评估） */
    static final String EVALUATION_DEBOUNCE_KEY_PREFIX = "ai:evaluation:done:";

    /** 出题难度轮换序列 */
    private static final String[] DIFFICULTY_CYCLE = {"easy", "medium", "hard"};

    private final AiQuizGeneratorTool quizGeneratorTool;
    private final LLMService llmService;
    private final org.springframework.data.redis.core.RedisTemplate<String, Object> redisTemplate;

    @Value("${app.ai.learning-completion.quiz-count:5}")
    private int quizCount;

    @Value("${app.ai.learning-completion.enabled:true}")
    private boolean enabled;

    @Async("ioTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLearningCompleted(LearningCompletedEvent event) {
        if (!enabled || event.userId() == null || event.chapterId() == null) {
            return;
        }
        try {
            generateQuizzes(event);
        } catch (Exception e) {
            log.error("学习完成自动出题失败 userId={} chapterId={}", event.userId(), event.chapterId(), e);
        }
        try {
            generateEvaluation(event);
        } catch (Exception e) {
            log.error("学习完成自动评估失败 userId={} chapterId={}", event.userId(), event.chapterId(), e);
        }
    }

    /**
     * 批量生成辅助题库；24 小时去抖窗口内重复完成不重复出题。
     */
    void generateQuizzes(LearningCompletedEvent event) {
        if (quizCount <= 0) {
            return;
        }
        String debounceKey = QUIZ_DEBOUNCE_KEY_PREFIX + event.userId() + ":" + event.chapterId();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(debounceKey, "1", java.time.Duration.ofHours(24));
        if (!Boolean.TRUE.equals(acquired)) {
            log.info("章节辅助题库近 24 小时已生成，跳过 userId={} chapterId={}", event.userId(), event.chapterId());
            return;
        }

        String chapterId = String.valueOf(event.chapterId());
        int success = 0;
        for (int i = 0; i < quizCount; i++) {
            try {
                quizGeneratorTool.generateMiniQuiz(
                        chapterId, null, "single_choice", DIFFICULTY_CYCLE[i % DIFFICULTY_CYCLE.length]);
                success++;
            } catch (Exception e) {
                // 单题失败不影响其余题目与后续动作
                log.warn("自动出题第 {}/{} 题失败 chapterId={}", i + 1, quizCount, chapterId, e);
            }
        }
        log.info("学习完成自动出题完成 userId={} chapterId={} 成功={}/{}", event.userId(), event.chapterId(), success, quizCount);
        if (success == 0) {
            // 全部失败视为本次生成未发生：回滚去抖键，避免一次瞬时故障锁死 24 小时无法重试
            redisTemplate.delete(debounceKey);
        }
    }

    /**
     * 生成本次学习评估报告；输出经 AI 记忆服务自动入库，供用户在会话中回看。
     *
     * <p>评估按 24 小时去抖：并发双发的完成事件只评估一次；生成失败时回滚去抖键，
     * 下次完成可重试。
     */
    void generateEvaluation(LearningCompletedEvent event) {
        String debounceKey = EVALUATION_DEBOUNCE_KEY_PREFIX + event.userId() + ":" + event.chapterId();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(debounceKey, "1", java.time.Duration.ofHours(24));
        if (!Boolean.TRUE.equals(acquired)) {
            log.info("章节学习评估近 24 小时已生成，跳过 userId={} chapterId={}", event.userId(), event.chapterId());
            return;
        }
        try {
            String prompt = "学员刚刚完成了章节（chapterId=%d）的学习。请以党建学习辅导老师的身份，"
                    + "为该学员生成本次学习的简要评估，包括：知识掌握情况小结、2-3 条针对性复习建议、"
                    + "下一步学习方向提示。请使用简体中文，控制在 300 字以内。".formatted(event.chapterId());
            AiChatRequest request =
                    new AiChatRequest("learning-eval-" + event.userId() + "-" + event.chapterId(), prompt);
            // 消费至流结束即触发会话存档（AiMemoryService.saveConversation）
            llmService.chatForUser(request, String.valueOf(event.userId())).blockLast(java.time.Duration.ofSeconds(90));
            log.info("学习完成自动评估已生成并存档 userId={} chapterId={}", event.userId(), event.chapterId());
        } catch (Exception e) {
            redisTemplate.delete(debounceKey);
            throw e;
        }
    }
}
