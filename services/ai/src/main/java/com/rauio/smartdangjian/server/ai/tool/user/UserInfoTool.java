package com.rauio.smartdangjian.server.ai.tool.user;

import com.rauio.smartdangjian.server.user.pojo.vo.UserVO;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;

@Component
public class UserInfoTool implements BiFunction<String, ToolContext,String> {

    /**
     * 从工具上下文中读取当前登录用户信息。
     *
     * @param toolContext 工具调用上下文
     * @return 当前用户视图对象
     */
    @Override
    public String apply(String s, ToolContext toolContext) {
        UserVO currentUser = (UserVO) toolContext.getContext().get("CURRENT_USER");
        if (currentUser == null) {
            throw new RuntimeException("用户未登录");
        }
        return currentUser.toString();
    }
}
