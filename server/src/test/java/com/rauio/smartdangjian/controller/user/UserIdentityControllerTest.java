package com.rauio.smartdangjian.controller.user;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.user.constants.UserErrorConstants;
import com.rauio.smartdangjian.server.user.controller.user.UserIdentityController;
import com.rauio.smartdangjian.server.user.pojo.response.UserResponse;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.utils.spec.UserType;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = UserIdentityControllerTest.TestConfig.class)
@DisplayName("当前用户身份接口测试")
class UserIdentityControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public UserIdentityController userIdentityController(UserService userService) {
            return new UserIdentityController(userService);
        }
    }

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("GET /me - 返回当前登录用户自身信息")
    void meSuccess() throws Exception {
        UserResponse vo = new UserResponse();
        vo.setId(1L);
        vo.setUsername("zhangsan");
        vo.setRealName("张三");
        vo.setUserType(UserType.STUDENT);
        when(userService.getCurrentUserProfile()).thenReturn(vo);

        mockMvc.perform(get("/api/user/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value("1"))
                .andExpect(jsonPath("$.data.username").value("zhangsan"))
                .andExpect(jsonPath("$.data.realName").value("张三"));
    }

    @Test
    @DisplayName("GET /me - 用户不存在时返回用户模块错误码")
    void meUserMissing() throws Exception {
        when(userService.getCurrentUserProfile())
                .thenThrow(new BusinessException(UserErrorConstants.USER_NOT_EXISTS, "用户不存在或登录状态已失效"));

        mockMvc.perform(get("/api/user/users/me"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(String.valueOf(UserErrorConstants.USER_NOT_EXISTS)))
                .andExpect(jsonPath("$.message").value("用户不存在或登录状态已失效"));
    }
}
