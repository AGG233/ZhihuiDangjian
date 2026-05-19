package com.rauio.smartdangjian.server.ai.tool;

import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import com.rauio.smartdangjian.server.ai.util.ToolContextUtil;
import com.rauio.smartdangjian.server.user.pojo.convertor.UserConvertor;
import com.rauio.smartdangjian.server.user.pojo.vo.UserVO;
import com.rauio.smartdangjian.server.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserInfoTool {

    private final UserService userService;
    private final UserConvertor userConvertor;

    @Tool(description = "获取用户基本信息")
    public UserVO getUserInfo(ToolContext toolContext) {
        String userId = ToolContextUtil.getUserId(toolContext, userService);
        return userConvertor.toVO(userService.getById(userId));
    }
}
