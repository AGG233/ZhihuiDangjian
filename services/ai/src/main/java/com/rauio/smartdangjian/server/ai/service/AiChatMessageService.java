package com.rauio.smartdangjian.server.ai.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.server.ai.mapper.AiChatMessageMapper;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiChatMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiChatMessageService extends ServiceImpl<AiChatMessageMapper, AiChatMessage> {

    @Transactional(readOnly = true)
    public AiChatMessage findLatestBySessionIdAndUserId(String sessionId, Long userId) {
        LambdaQueryWrapper<AiChatMessage> wrapper = new LambdaQueryWrapper<AiChatMessage>()
                .eq(AiChatMessage::getSessionId, sessionId)
                .eq(AiChatMessage::getUserId, userId)
                .orderByDesc(AiChatMessage::getCreatedAt);
        return this.page(new Page<>(1, 1), wrapper).getRecords().stream()
                .findFirst()
                .orElse(null);
    }
}
