package com.rauio.smartdangjian.event;

import java.time.LocalDateTime;

/**
 * 章节学习完成事件。
 *
 * <p>由 learning 模块在学习进度首次达到 100%（completed）时发布，
 * 下游模块（如 AI 的自动出题、学习评估）以 AFTER_COMMIT 异步监听，
 * 不阻塞学习主链路。定义在 common 以避免模块间反向依赖。
 */
public record LearningCompletedEvent(Long userId, Long chapterId, LocalDateTime completedAt) {}
