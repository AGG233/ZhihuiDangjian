package com.rauio.smartdangjian.server.ai.tool;

import com.rauio.smartdangjian.server.user.pojo.convertor.UserConvertor;
import com.rauio.smartdangjian.server.user.pojo.vo.UserVO;
import com.rauio.smartdangjian.server.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserInfoTool {

    private final UserService userService;
    private final UserConvertor userConvertor;

    @Tool(description = "获取用户基本信息")
    public UserVO getUserInfo() {
        return userConvertor.toVO(userService.getCurrentUser());
    }
}
