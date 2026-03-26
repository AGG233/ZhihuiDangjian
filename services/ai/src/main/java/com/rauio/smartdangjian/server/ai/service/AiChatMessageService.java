package com.rauio.smartdangjian.server.ai.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.ai.mapper.AiChatMessageMapper;
import com.rauio.smartdangjian.server.ai.pojo.entity.AiChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;


@Service
@RequiredArgsConstructor
@Transactional
public class AiChatMessageService extends ServiceImpl<AiChatMessageMapper, AiChatMessage> {

    /**
     * 根据消息 ID 获取会话消息。
     *
     * @param messageId 消息 ID
     * @return 会话消息
     */
    public AiChatMessage get(String messageId) {
        if (messageId == null) {
            throw new BusinessException(ErrorConstants.ARGS_ERROR, "messageId不能为空");
        }
        AiChatMessage message = this.getById(messageId);
        if (message == null) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_EXISTS, "消息不存在");
        }
        return message;
    }

    /**
     * 创建会话消息。
     *
     * @param message 会话消息实体
     * @return 是否创建成功
     */
    public Boolean create(AiChatMessage message) {
        if (message == null) {
            throw new BusinessException(ErrorConstants.ARGS_ERROR, "消息不能为空");
        }
        if (message.getUserId() == null) {
            throw new BusinessException(ErrorConstants.ARGS_ERROR, "userId不能为空");
        }
        if (!this.save(message)) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AVAILABLE, "消息创建失败");
        }
        return true;
    }

    /**
     * 使用问答内容创建一条会话消息。
     *
     * @param userId 用户 ID
     * @param finalInput 最终输入内容
     * @param finalOutput 最终输出内容
     * @return 是否创建成功
     */
    public Boolean create(String userId,String finalInput,String finalOutput) {
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("finalInput", finalInput);
        metadata.put("finalOutput", finalOutput);

        return create(
                AiChatMessage.builder()
                .content(finalOutput)
                .userId(userId)
                .messageType("text")
                .metadata(metadata)
                .senderType("user")
                .build()
        );
    }

    /**
     * 更新会话消息。
     *
     * @param message 会话消息实体
     * @return 是否更新成功
     */
    public Boolean update(AiChatMessage message) {
        if (message == null || message.getId() == null) {
            throw new BusinessException(ErrorConstants.ARGS_ERROR, "messageId不能为空");
        }
        if (message.getUserId() == null) {
            throw new BusinessException(ErrorConstants.ARGS_ERROR, "userId不能为空");
        }
        if (!this.updateById(message)) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AVAILABLE, "消息更新失败");
        }
        return true;
    }

    /**
     * 删除会话消息。
     *
     * @param messageId 消息 ID
     * @return 是否删除成功
     */
    public Boolean delete(String messageId) {
        if (messageId == null) {
            throw new BusinessException(ErrorConstants.ARGS_ERROR, "messageId不能为空");
        }
        if (!this.removeById(messageId)) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AVAILABLE, "消息删除失败");
        }
        return true;
    }

}
