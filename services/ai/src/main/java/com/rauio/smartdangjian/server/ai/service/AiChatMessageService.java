package com.rauio.smartdangjian.server.ai.service;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.server.ai.mapper.AiChatMessageMapper;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiChatMessage;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AiChatMessageService extends ServiceImpl<AiChatMessageMapper, AiChatMessage> {}
