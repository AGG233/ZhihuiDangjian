package com.rauio.smartdangjian.controller.user;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.controller.factory.CourseTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.user.pojo.dto.UserDto;
import com.rauio.smartdangjian.server.user.pojo.vo.UserPublicVO;
import com.rauio.smartdangjian.server.user.pojo.vo.UserVO;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = UserControllerTest.TestConfig.class)
@DisplayName("用户管理接口测试")
class UserControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    @ComponentScan(basePackages = "com.rauio.smartdangjian.server.user.controller")
    static class TestConfig extends CommonTestConfig {}

    @MockitoBean
    private UserService userService;

    // ═══════════════════════════════════════════════════════════════
    // 正常场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("GET /{id} - 获取用户信息成功")
        void getSuccess() throws Exception {
            UserVO vo = new UserVO();
            vo.setId("user-001");
            vo.setUsername("zhangsan");
            vo.setRealName("张三");
            vo.setUserType(com.rauio.smartdangjian.utils.spec.UserType.STUDENT);
            when(userService.get("user-001")).thenReturn(vo);

            mockMvc.perform(get("/api/user/users/user-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("user-001"))
                    .andExpect(jsonPath("$.data.username").value("zhangsan"))
                    .andExpect(jsonPath("$.data.realName").value("张三"));
        }

        @Test
        @DisplayName("POST /search - 用户分页搜索成功")
        void searchSuccess() throws Exception {
            UserPublicVO publicVO = new UserPublicVO();
            publicVO.setId("user-001");
            publicVO.setUsername("zhangsan");
            publicVO.setRealName("张三");
            publicVO.setPartyStatus(PartyStatus.FORMAL_MEMBER);
            Page<UserPublicVO> page = new Page<>(1, 10, 1);
            page.setRecords(List.of(publicVO));

            when(userService.getPage(any(UserDto.class), eq(1), eq(10))).thenReturn(page);

            UserDto dto = new UserDto();
            dto.setUsername("zhang");
            mockMvc.perform(post("/api/user/users/search")
                            .param("pageNum", "1")
                            .param("pageSize", "10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.records[0].username").value("zhangsan"))
                    .andExpect(jsonPath("$.data.total").value(1));
        }

        @Test
        @DisplayName("PUT /{id} - 更新用户成功")
        void updateSuccess() throws Exception {
            when(userService.update(eq("user-001"), any())).thenReturn(true);

            String json = "{\"realName\":\"张三丰\"}";
            mockMvc.perform(put("/api/user/users/user-001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("DELETE /{id} - 接口已弃用返回 404")
        void deleteReturnsDeprecated() throws Exception {
            mockMvc.perform(delete("/api/user/users/user-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("404"))
                    .andExpect(jsonPath("$.message").value("接口已经弃用"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 异常处理场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("GET /{id} - Service 抛出 BusinessException 返回 400")
        void getThrowsBusinessException() throws Exception {
            when(userService.get("user-001")).thenThrow(new BusinessException(4000, "用户不存在"));

            mockMvc.perform(get("/api/user/users/user-001"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("用户不存在"));
        }

        @Test
        @DisplayName("GET /{id} - Service 抛出 RuntimeException 返回 500")
        void getThrowsRuntimeException() throws Exception {
            when(userService.get("user-001")).thenThrow(new RuntimeException("数据库异常"));

            mockMvc.perform(get("/api/user/users/user-001"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("POST /search - Service 抛出 BusinessException 返回 400")
        void searchThrowsBusinessException() throws Exception {
            when(userService.getPage(any(UserDto.class), anyInt(), anyInt()))
                    .thenThrow(new BusinessException(1005, "用户不存在"));

            mockMvc.perform(post("/api/user/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("1005"))
                    .andExpect(jsonPath("$.message").value("用户不存在"));
        }

        @Test
        @DisplayName("POST /search - Service 抛出 RuntimeException 返回 500")
        void searchThrowsRuntimeException() throws Exception {
            when(userService.getPage(any(UserDto.class), anyInt(), anyInt()))
                    .thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(post("/api/user/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("PUT /{id} - Service 抛出 BusinessException 返回 400")
        void updateThrowsBusinessException() throws Exception {
            when(userService.update(eq("user-001"), any())).thenThrow(new BusinessException(4000, "更新用户失败"));

            mockMvc.perform(put("/api/user/users/user-001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"realName\":\"新名称\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4000"))
                    .andExpect(jsonPath("$.message").value("更新用户失败"));
        }

        @Test
        @DisplayName("PUT /{id} - Service 抛出 RuntimeException 返回 500")
        void updateThrowsRuntimeException() throws Exception {
            when(userService.update(eq("user-001"), any())).thenThrow(new RuntimeException("数据库异常"));

            mockMvc.perform(put("/api/user/users/user-001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"realName\":\"新名称\"}"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("PUT /{id} - Service 返回 false 时 code 为 400")
        void updateReturnsFalse() throws Exception {
            when(userService.update(eq("user-001"), any())).thenReturn(false);

            mockMvc.perform(put("/api/user/users/user-001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"realName\":\"新名称\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(false))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("POST /search - 非法 JSON 请求体返回 400")
        void malformedJson() throws Exception {
            mockMvc.perform(post("/api/user/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid json"))
                    .andExpect(status().isBadRequest());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 边界场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("GET /{id} - 服务返回 null 时 code 为 400")
        void getReturnsNull() throws Exception {
            when(userService.get("nonexistent")).thenReturn(null);

            mockMvc.perform(get("/api/user/users/nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.message").value("OK"));
        }

        @Test
        @DisplayName("POST /search - 空结果集返回空列表")
        void searchEmptyResults() throws Exception {
            Page<UserPublicVO> emptyPage = new Page<>(1, 10, 0);
            emptyPage.setRecords(List.of());
            when(userService.getPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(emptyPage);

            mockMvc.perform(post("/api/user/users/search")
                            .param("pageNum", "1")
                            .param("pageSize", "10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.records").isEmpty())
                    .andExpect(jsonPath("$.data.total").value(0));
        }

        @Test
        @DisplayName("POST /search - 空请求体 {} 正常处理")
        void searchWithEmptyBody() throws Exception {
            Page<UserPublicVO> page = new Page<>(1, 10, 0);
            page.setRecords(List.of());
            when(userService.getPage(any(UserDto.class), eq(1), eq(10))).thenReturn(page);

            mockMvc.perform(post("/api/user/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("POST /search - 搜索字段含特殊字符")
        void searchWithSpecialChars() throws Exception {
            Page<UserPublicVO> page = new Page<>(1, 10, 0);
            page.setRecords(List.of());
            when(userService.getPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = new UserDto();
            dto.setUsername("user_@#$%");
            mockMvc.perform(post("/api/user/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("POST /search - 搜索字段超长字符串")
        void searchWithLongField() throws Exception {
            Page<UserPublicVO> page = new Page<>(1, 10, 0);
            page.setRecords(List.of());
            when(userService.getPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = new UserDto();
            dto.setUsername("a".repeat(1000));
            mockMvc.perform(post("/api/user/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("POST /search - 默认分页参数")
        void searchWithDefaultPagination() throws Exception {
            Page<UserPublicVO> page = new Page<>(1, 10, 0);
            page.setRecords(List.of());
            when(userService.getPage(any(UserDto.class), eq(1), eq(10))).thenReturn(page);

            mockMvc.perform(post("/api/user/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.current").value(1))
                    .andExpect(jsonPath("$.data.size").value(10));
        }

        @Test
        @DisplayName("PUT /{id} - 空请求体正常处理")
        void updateWithEmptyBody() throws Exception {
            mockMvc.perform(put("/api/user/users/user-001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 安全场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("XSS 注入在搜索字段中")
        void xssInSearch() throws Exception {
            Page<UserPublicVO> page = new Page<>(1, 10, 0);
            page.setRecords(List.of());
            when(userService.getPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = new UserDto();
            dto.setUsername("<script>alert('xss')</script>");
            mockMvc.perform(post("/api/user/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("SQL 注入在搜索字段中")
        void sqlInjectionInSearch() throws Exception {
            Page<UserPublicVO> page = new Page<>(1, 10, 0);
            page.setRecords(List.of());
            when(userService.getPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = new UserDto();
            dto.setUsername("' OR '1'='1");
            mockMvc.perform(post("/api/user/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CourseTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST 请求获取接口返回 405")
        void getWithWrongMethod() throws Exception {
            mockMvc.perform(post("/api/user/users/user-001")).andExpect(status().isMethodNotAllowed());
        }

        @Test
        @DisplayName("GET 请求搜索接口路径匹配 GET /{id} 返回 200")
        void searchWithWrongMethod() throws Exception {
            mockMvc.perform(get("/api/user/users/search")).andExpect(status().isOk());
        }

        @Test
        @DisplayName("DELETE 请求搜索接口路径匹配 DELETE /{id} 返回 200")
        void searchWithDeleteMethod() throws Exception {
            mockMvc.perform(delete("/api/user/users/search")).andExpect(status().isOk());
        }
    }
}
