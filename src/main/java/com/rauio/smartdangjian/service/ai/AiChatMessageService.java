package com.rauio.smartdangjian.service.ai;

import com.rauio.smartdangjian.aop.annotation.PermissionAccess;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.dao.AiChatMessageDao;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.pojo.AiChatMessage;
import com.rauio.smartdangjian.service.user.UserService;
import com.rauio.smartdangjian.utils.spec.UserType;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;


@Service
@RequiredArgsConstructor
@Transactional
public class AiChatMessageService {

    private final AiChatMessageDao aiChatMessageDao;

    public AiChatMessage get(String messageId) {
        if (messageId == null) {
            throw new BusinessException(ErrorConstants.ARGS_ERROR, "messageId不能为空");
        }
        AiChatMessage message = aiChatMessageDao.get(messageId);
        if (message == null) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_EXISTS, "消息不存在");
        }
        return message;
    }

    public Boolean create(AiChatMessage message) {
        if (message == null) {
            throw new BusinessException(ErrorConstants.ARGS_ERROR, "消息不能为空");
        }
        if (message.getUserId() == null) {
            throw new BusinessException(ErrorConstants.ARGS_ERROR, "userId不能为空");
        }
        if (!aiChatMessageDao.insert(message)) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AVAILABLE, "消息创建失败");
        }
        return true;
    }

    public Boolean create(Long userId,String finalInput,String finalOutput) {
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

    public Boolean update(AiChatMessage message) {
        if (message == null || message.getId() == null) {
            throw new BusinessException(ErrorConstants.ARGS_ERROR, "messageId不能为空");
        }
        if (message.getUserId() == null) {
            throw new BusinessException(ErrorConstants.ARGS_ERROR, "userId不能为空");
        }
        if (!aiChatMessageDao.update(message)) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AVAILABLE, "消息更新失败");
        }
        return true;
    }

    @PermissionAccess(UserType.MANAGER)
    public Boolean delete(String messageId) {
        if (messageId == null) {
            throw new BusinessException(ErrorConstants.ARGS_ERROR, "messageId不能为空");
        }
        if (!aiChatMessageDao.delete(messageId)) {
            throw new BusinessException(ErrorConstants.RESOURCE_NOT_AVAILABLE, "消息删除失败");
        }
        return true;
    }

}
