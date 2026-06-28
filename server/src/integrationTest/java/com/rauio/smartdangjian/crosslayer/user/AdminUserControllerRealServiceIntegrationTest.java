package com.rauio.smartdangjian.crosslayer.user;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.user.controller.admin.AdminUserController;
import com.rauio.smartdangjian.server.user.controller.user.UserController;
import com.rauio.smartdangjian.server.user.pojo.request.UserRequest;
import com.rauio.smartdangjian.server.user.pojo.request.UserUpdateRequest;
import com.rauio.smartdangjian.server.user.pojo.response.UserPublicResponse;
import com.rauio.smartdangjian.server.user.pojo.response.UserResponse;
import com.rauio.smartdangjian.server.user.service.UserService;

@SpringBootTest(classes = AdminUserControllerRealServiceIntegrationTest.TestConfig.class)
@DisplayName("管理员和用户管理控制层集成测试")
class AdminUserControllerRealServiceIntegrationTest extends CrossLayerTestBase {

    @MockitoBean
    private UserService userService;

    @BeforeEach
    void setUp() {
        reset(userService);
    }

    // ==================== AdminUserController (SCHOOL) ====================

    @Test
    @DisplayName("Admin: GET /api/admin/users/{id} SCHOOL 用户成功获取用户详情")
    void adminGetUser() throws Exception {
        setSchoolContext(1L, "uni-1");
        UserResponse response = new UserResponse();
        response.setId(100L);
        response.setUsername("testuser");
        response.setRealName("测试用户");
        when(userService.get(100L)).thenReturn(response);

        mockMvc.perform(get("/api/admin/users/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService).get(100L);
    }

    @Test
    @DisplayName("Admin: POST /api/admin/users/search 成功分页搜索用户")
    void adminSearchUsers() throws Exception {
        setSchoolContext(1L, "uni-1");
        Page<UserResponse> page = new Page<>(1, 10);
        UserResponse user = new UserResponse();
        user.setId(1L);
        user.setUsername("user1");
        page.setRecords(List.of(user));
        page.setTotal(1);
        when(userService.getAdminResponsePage(any(UserRequest.class), anyInt(), anyInt()))
                .thenReturn(page);

        mockMvc.perform(post("/api/admin/users/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user1\"}")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].username").value("user1"));
    }

    @Test
    @DisplayName("Admin: POST /api/admin/users 成功创建用户")
    void adminCreateUser() throws Exception {
        setSchoolContext(1L, "uni-1");

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"test\",\"realName\":\"Test\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        verify(userService).register(any(UserRequest.class));
    }

    @Test
    @DisplayName("Admin: PUT /api/admin/users/{id} 成功更新用户")
    void adminUpdateUser() throws Exception {
        setSchoolContext(1L, "uni-1");

        mockMvc.perform(put("/api/admin/users/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"updated\",\"realName\":\"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        verify(userService).update(anyLong(), any(UserRequest.class));
    }

    @Test
    @DisplayName("Admin: DELETE /api/admin/users/{id} 成功删除用户")
    void adminDeleteUser() throws Exception {
        setSchoolContext(1L, "uni-1");

        mockMvc.perform(delete("/api/admin/users/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        verify(userService).delete(100L);
    }

    // ==================== UserController (STUDENT) ====================

    @Test
    @DisplayName("User: GET /api/user/users/me 成功获取当前用户")
    void userGetMe() throws Exception {
        setStudentContext(1L, "uni-1");
        UserResponse response = new UserResponse();
        response.setId(1L);
        response.setUsername("self");
        response.setRealName("我自己");
        when(userService.get(1L)).thenReturn(response);
        when(userService.getCurrentUserId()).thenReturn("1");

        mockMvc.perform(get("/api/user/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.username").value("self"));
    }

    @Test
    @DisplayName("User: GET /api/user/users/{id} 成功获取用户公开信息")
    void userGetById() throws Exception {
        setStudentContext(2L, "uni-1");
        UserResponse response = new UserResponse();
        response.setId(2L);
        response.setUsername("other");
        response.setRealName("其他用户");
        when(userService.get(2L)).thenReturn(response);

        mockMvc.perform(get("/api/user/users/2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.username").value("other"));
    }

    @Test
    @DisplayName("User: POST /api/user/users/search 成功分页搜索公开用户")
    void userSearch() throws Exception {
        setStudentContext(1L, "uni-1");
        Page<UserPublicResponse> page = new Page<>(1, 10);
        UserPublicResponse user = new UserPublicResponse();
        user.setId(1L);
        user.setUsername("publicUser");
        page.setRecords(List.of(user));
        page.setTotal(1);
        when(userService.getPage(any(UserRequest.class), anyInt(), anyInt())).thenReturn(page);

        mockMvc.perform(post("/api/user/users/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"publicUser\"}")
                        .param("pageNum", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].username").value("publicUser"));
    }

    @Test
    @DisplayName("User: PUT /api/user/users/{id} 成功更新用户信息")
    void userUpdate() throws Exception {
        setStudentContext(1L, "uni-1");

        mockMvc.perform(put("/api/user/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"realName\":\"新名字\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"));

        verify(userService).update(anyLong(), any(UserUpdateRequest.class));
    }

    @Test
    @DisplayName("User: DELETE /api/user/users/{id} 已弃用返回404")
    void userDeleteDeprecated() throws Exception {
        setStudentContext(1L, "uni-1");

        mockMvc.perform(delete("/api/user/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("404"))
                .andExpect(jsonPath("$.message").value("接口已经弃用"));
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        AdminUserController adminUserController(UserService userService) {
            return new AdminUserController(userService);
        }

        @Bean
        UserController userController(UserService userService) {
            return new UserController(userService);
        }

        @Bean
        AbstractPlatformTransactionManager transactionManager() {
            return new AbstractPlatformTransactionManager() {
                @Override
                protected Object doGetTransaction() {
                    return new Object();
                }

                @Override
                protected void doBegin(Object transaction, TransactionDefinition definition) {}

                @Override
                protected void doCommit(DefaultTransactionStatus status) {}

                @Override
                protected void doRollback(DefaultTransactionStatus status) {}
            };
        }
    }
}
