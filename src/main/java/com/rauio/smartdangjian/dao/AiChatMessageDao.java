package com.rauio.smartdangjian.dao;

import com.rauio.smartdangjian.mapper.AiChatMessageMapper;
import com.rauio.smartdangjian.pojo.AiChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class AiChatMessageDao {

    private final AiChatMessageMapper aiChatMessageMapper;

    @Autowired
    public AiChatMessageDao(AiChatMessageMapper aiChatMessageMapper) {
        this.aiChatMessageMapper = aiChatMessageMapper;
    }

    public AiChatMessage get(String messageId) {
        return aiChatMessageMapper.selectById(messageId);
    }

    public Boolean update(AiChatMessage aiChatMessage) {
        return aiChatMessageMapper.updateById(aiChatMessage) > 0;
    }

    public Boolean insert(AiChatMessage aiChatMessage) {
        return aiChatMessageMapper.insert(aiChatMessage) > 0;
    }

    public Boolean delete(String messageId) {
        return aiChatMessageMapper.deleteById(messageId) > 0;
    }
}