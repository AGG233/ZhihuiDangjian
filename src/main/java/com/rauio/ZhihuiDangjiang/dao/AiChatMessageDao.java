package com.rauio.ZhihuiDangjiang.dao;

import com.rauio.ZhihuiDangjiang.mapper.AiChatMessageMapper;
import com.rauio.ZhihuiDangjiang.pojo.AiChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AiChatMessageDao {

    private final AiChatMessageMapper aiChatMessageMapper;

    @Autowired
    public AiChatMessageDao(AiChatMessageMapper aiChatMessageMapper) {
        this.aiChatMessageMapper = aiChatMessageMapper;
    }

    public AiChatMessage get(Long messageId) {
        return aiChatMessageMapper.selectById(messageId);
    }

    public Boolean update(AiChatMessage aiChatMessage) {
        return aiChatMessageMapper.updateById(aiChatMessage) > 0;
    }

    public Boolean insert(AiChatMessage aiChatMessage) {
        return aiChatMessageMapper.insert(aiChatMessage) > 0;
    }

    public Boolean delete(Long messageId) {
        return aiChatMessageMapper.deleteById(messageId) > 0;
    }
}