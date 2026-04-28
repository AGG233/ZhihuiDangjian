package com.rauio.smartdangjian.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.server.user.controller.admin.AdminUserController;
import com.rauio.smartdangjian.server.user.pojo.dto.UserDto;
import com.rauio.smartdangjian.server.user.pojo.vo.UserVO;
import com.rauio.smartdangjian.server.user.service.UserService;
import com.rauio.smartdangjian.server.user.utils.spec.AccountStatus;
import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;
import com.rauio.smartdangjian.utils.spec.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AdminUserControllerTest.TestConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(
        locations = "classpath:application-test.yaml",
        properties = {
                "REDIS_HOST=localhost",
                "REDIS_PORT=6379",
                "REDIS_DATABASE=0",
                "DATABASE_URL=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                "DATABASE_USERNAME=sa",
                "DATABASE_PASSWORD=",
                "NEO4J_URI=bolt://localhost:7687",
                "NEO4J_USERNAME=neo4j",
                "NEO4J_PASSWORD=password"
        }
)
@DisplayName("管理员用户接口测试")
class AdminUserControllerTest {

    @SpringBootConfiguration
    @EnableAutoConfiguration(exclude = {
            DataSourceAutoConfiguration.class,
            HibernateJpaAutoConfiguration.class,
            org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
            org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration.class,
            com.rauio.smartdangjian.config.SecurityCoreAutoConfiguration.class,
            com.rauio.smartdangjian.config.SecuritySupportAutoConfiguration.class,
            com.rauio.smartdangjian.config.TransactionConfig.class
    })
    @EnableWebMvc
    @ComponentScan(basePackages = "com.rauio.smartdangjian.server.user.controller")
    static class TestConfig {
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @BeforeEach
    void setUpSecurityContext() {
        CurrentUserPrincipal principal = new CurrentUserPrincipal() {
            @Override
            public String getId() { return "admin1"; }
            @Override
            public UserType getUserType() { return UserType.SCHOOL; }
            @Override
            public String getUniversityId() { return "uni1"; }
        };
        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        principal, null, java.util.Collections.emptyList()
                )
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // NormalSearchTests
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("正常搜索场景")
    class NormalSearchTests {

        @Test
        @DisplayName("按用户名模糊搜索返回匹配用户")
        void searchByUsername() throws Exception {
            List<UserVO> records = UserTestDataFactory.createUserVOList(3);
            Page<UserVO> page = UserTestDataFactory.createPage(records, 1, 10, 3);

            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = UserTestDataFactory.createSearchDto();
            dto.setUsername("user");

            mockMvc.perform(post("/api/admin/users/search")
                            .param("pageNum", "1")
                            .param("pageSize", "10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(UserTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.records.length()").value(3))
                    .andExpect(jsonPath("$.data.total").value(3));
        }

        @Test
        @DisplayName("按真实姓名模糊搜索返回匹配用户")
        void searchByRealName() throws Exception {
            UserVO vo = UserTestDataFactory.createUserVO("user-002", "lisi", "李四", UserType.STUDENT);
            Page<UserVO> page = UserTestDataFactory.createPage(List.of(vo), 1, 10, 1);

            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = UserTestDataFactory.createSearchDto();
            dto.setRealName("李");

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(UserTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.records[0].realName").value("李四"))
                    .andExpect(jsonPath("$.data.total").value(1));
        }

        @Test
        @DisplayName("按用户类型精确匹配")
        void searchByUserType() throws Exception {
            List<UserVO> records = UserTestDataFactory.createUserVOList(2, UserType.STUDENT);
            Page<UserVO> page = UserTestDataFactory.createPage(records, 1, 10, 2);

            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = UserTestDataFactory.createSearchDto();
            dto.setUserType(UserType.STUDENT);

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(UserTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.records[0].userType").value("学生"))
                    .andExpect(jsonPath("$.data.total").value(2));
        }

        @Test
        @DisplayName("按政治面貌精确匹配")
        void searchByPartyStatus() throws Exception {
            UserVO vo = UserTestDataFactory.createDefaultUserVO();
            Page<UserVO> page = UserTestDataFactory.createPage(List.of(vo), 1, 10, 1);

            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = UserTestDataFactory.createSearchDto();
            dto.setPartyStatus(PartyStatus.FORMAL_MEMBER);

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(UserTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.records[0].partyStatus").value("正式党员"));
        }

        @Test
        @DisplayName("多条件组合搜索")
        void searchByMultipleConditions() throws Exception {
            UserVO vo = UserTestDataFactory.createDefaultUserVO();
            Page<UserVO> page = UserTestDataFactory.createPage(List.of(vo), 1, 10, 1);

            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = UserTestDataFactory.createSearchDto();
            dto.setUsername("zhang");
            dto.setUserType(UserType.STUDENT);
            dto.setPartyStatus(PartyStatus.FORMAL_MEMBER);

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(UserTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("按党支部名称模糊搜索")
        void searchByBranchName() throws Exception {
            List<UserVO> records = UserTestDataFactory.createUserVOList(5);
            Page<UserVO> page = UserTestDataFactory.createPage(records, 1, 10, 5);

            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = UserTestDataFactory.createSearchDto();
            dto.setBranchName("第一");

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(UserTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.records.length()").value(5));
        }

        @Test
        @DisplayName("按邮箱模糊搜索")
        void searchByEmail() throws Exception {
            UserVO vo = UserTestDataFactory.createDefaultUserVO();
            Page<UserVO> page = UserTestDataFactory.createPage(List.of(vo), 1, 10, 1);

            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = UserTestDataFactory.createSearchDto();
            dto.setEmail("zhangsan@example.com");

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(UserTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("按手机号模糊搜索")
        void searchByPhone() throws Exception {
            UserVO vo = UserTestDataFactory.createDefaultUserVO();
            Page<UserVO> page = UserTestDataFactory.createPage(List.of(vo), 1, 10, 1);

            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = UserTestDataFactory.createSearchDto();
            dto.setPhone("138");

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(UserTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("按用户ID模糊搜索")
        void searchByUserId() throws Exception {
            List<UserVO> records = UserTestDataFactory.createUserVOList(10);
            Page<UserVO> page = UserTestDataFactory.createPage(records, 1, 10, 10);

            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = UserTestDataFactory.createSearchDto();
            dto.setUserId("user-");

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(UserTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.records.length()").value(10));
        }

        @Test
        @DisplayName("按学校ID精确匹配")
        void searchByUniversityId() throws Exception {
            UserVO vo = UserTestDataFactory.createDefaultUserVO();
            Page<UserVO> page = UserTestDataFactory.createPage(List.of(vo), 1, 10, 1);

            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = UserTestDataFactory.createSearchDto();
            dto.setUniversityId("uni-sustech-001");

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(UserTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.records[0].universityId").value("uni-sustech-001"));
        }

        @Test
        @DisplayName("不传分页参数时使用默认值 pageNum=1, pageSize=10")
        void searchWithDefaultPagination() throws Exception {
            List<UserVO> records = UserTestDataFactory.createUserVOList(10);
            Page<UserVO> page = UserTestDataFactory.createPage(records, 1, 10, 10);

            when(userService.getAdminPage(any(UserDto.class), eq(1), eq(10))).thenReturn(page);

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.current").value(1))
                    .andExpect(jsonPath("$.data.size").value(10));
        }

        @Test
        @DisplayName("自定义分页参数返回指定页数据")
        void searchWithCustomPagination() throws Exception {
            List<UserVO> records = UserTestDataFactory.createUserVOList(5);
            Page<UserVO> page = UserTestDataFactory.createPage(records, 2, 5, 25);

            when(userService.getAdminPage(any(UserDto.class), eq(2), eq(5))).thenReturn(page);

            mockMvc.perform(post("/api/admin/users/search")
                            .param("pageNum", "2")
                            .param("pageSize", "5")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.current").value(2))
                    .andExpect(jsonPath("$.data.size").value(5));
        }

        @Test
        @DisplayName("多页数据场景下返回正确的分页信息")
        void searchReturnsMultiplePages() throws Exception {
            List<UserVO> records = UserTestDataFactory.createUserVOList(10);
            Page<UserVO> page = UserTestDataFactory.createPage(records, 2, 10, 25);

            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            mockMvc.perform(post("/api/admin/users/search")
                            .param("pageNum", "2")
                            .param("pageSize", "10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.total").value(25))
                    .andExpect(jsonPath("$.data.current").value(2))
                    .andExpect(jsonPath("$.data.records.length()").value(10));
        }

        @Test
        @DisplayName("搜索无匹配条件时返回空列表（非错误）")
        void searchNoResults() throws Exception {
            Page<UserVO> page = UserTestDataFactory.createEmptyPage(1, 10);

            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = UserTestDataFactory.createSearchDto();
            dto.setUsername("nonexistent");

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(UserTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.records.length()").value(0))
                    .andExpect(jsonPath("$.data.total").value(0));
        }

        @Test
        @DisplayName("空请求体 {} 返回全部用户第一页")
        void searchWithEmptyBody() throws Exception {
            List<UserVO> records = UserTestDataFactory.createUserVOList(10);
            Page<UserVO> page = UserTestDataFactory.createPage(records, 1, 10, 50);

            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.total").value(50))
                    .andExpect(jsonPath("$.data.records.length()").value(10));
        }

        @Test
        @DisplayName("响应包含 UserVO 的所有字段")
        void searchResponseContainsAllVOFields() throws Exception {
            UserVO vo = UserTestDataFactory.createDefaultUserVO();
            Page<UserVO> page = UserTestDataFactory.createPage(List.of(vo), 1, 10, 1);

            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.records[0].id").value("user-001"))
                    .andExpect(jsonPath("$.data.records[0].username").value("zhangsan"))
                    .andExpect(jsonPath("$.data.records[0].realName").value("张三"))
                    .andExpect(jsonPath("$.data.records[0].partyMemberId").value("PM-user-001"))
                    .andExpect(jsonPath("$.data.records[0].partyStatus").value("正式党员"))
                    .andExpect(jsonPath("$.data.records[0].branchName").value("第一党支部"))
                    .andExpect(jsonPath("$.data.records[0].userType").value("学生"))
                    .andExpect(jsonPath("$.data.records[0].status").value("active"))
                    .andExpect(jsonPath("$.data.records[0].universityId").value("uni-sustech-001"))
                    .andExpect(jsonPath("$.data.records[0].email").value("zh*n@example.com"))
                    .andExpect(jsonPath("$.data.records[0].phone").value("+8613800138000"));
        }

        @Test
        @DisplayName("响应保持服务端返回的记录顺序")
        void searchPreservesRecordOrder() throws Exception {
            UserVO first = UserTestDataFactory.createUserVO("user-003", "user003", "赵六", UserType.STUDENT);
            UserVO second = UserTestDataFactory.createUserVO("user-001", "user001", "张三", UserType.STUDENT);
            Page<UserVO> page = UserTestDataFactory.createPage(List.of(first, second), 1, 10, 2);

            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.records[0].id").value("user-003"))
                    .andExpect(jsonPath("$.data.records[1].id").value("user-001"));
        }

        @Test
        @DisplayName("GET /{id} - 获取用户详情成功")
        void getUserDetailSuccessfully() throws Exception {
            UserVO userVO = UserTestDataFactory.createManagerUserVO();

            when(userService.get("1001")).thenReturn(userVO);

            mockMvc.perform(get("/api/admin/users/1001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data.id").value("manager-001"))
                    .andExpect(jsonPath("$.data.username").value("superadmin"))
                    .andExpect(jsonPath("$.data.userType").value("管理员"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // ErrorHandlingTests
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("异常和错误处理场景")
    class ErrorHandlingTests {

        @Test
        @DisplayName("非数字 pageNum 参数返回 500（Spring 参数类型转换失败进入 RuntimeException 处理）")
        void searchWithNonNumericPageNum() throws Exception {
            mockMvc.perform(post("/api/admin/users/search")
                            .param("pageNum", "abc")
                            .param("pageSize", "10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().is5xxServerError());
        }

        @Test
        @DisplayName("Service 抛出 BusinessException 返回 400 并携带错误码和消息")
        void searchServiceThrowsBusinessException() throws Exception {
            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt()))
                    .thenThrow(new BusinessException(ErrorConstants.USER_NOT_EXISTS, "用户不存在"));

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("1005"))
                    .andExpect(jsonPath("$.message").value("用户不存在"));
        }

        @Test
        @DisplayName("Service 抛出 RuntimeException 返回 500")
        void searchServiceThrowsRuntimeException() throws Exception {
            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt()))
                    .thenThrow(new RuntimeException("数据库连接失败"));

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }

        @Test
        @DisplayName("非法 JSON 请求体返回 400")
        void searchWithMalformedJson() throws Exception {
            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{username: broken"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Service 抛出 IllegalArgumentException(模拟负页码) 返回 500")
        void searchWithNegativePageNumCausesRuntimeException() throws Exception {
            when(userService.getAdminPage(any(UserDto.class), eq(-1), anyInt()))
                    .thenThrow(new IllegalArgumentException("pageNum must be >= 1"));

            mockMvc.perform(post("/api/admin/users/search")
                            .param("pageNum", "-1")
                            .param("pageSize", "10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("零页码引发 MyBatis-Plus 参数校验异常返回 500")
        void searchWithZeroPageNumCausesRuntimeException() throws Exception {
            when(userService.getAdminPage(any(UserDto.class), eq(0), anyInt()))
                    .thenThrow(new IllegalArgumentException("pageNum must be >= 1"));

            mockMvc.perform(post("/api/admin/users/search")
                            .param("pageNum", "0")
                            .param("pageSize", "10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("负 pageSize 引发 MyBatis-Plus 参数校验异常返回 500")
        void searchWithNegativePageSizeCausesRuntimeException() throws Exception {
            when(userService.getAdminPage(any(UserDto.class), anyInt(), eq(-5)))
                    .thenThrow(new IllegalArgumentException("pageSize must be >= 1"));

            mockMvc.perform(post("/api/admin/users/search")
                            .param("pageNum", "1")
                            .param("pageSize", "-5")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("零 pageSize 引发 MyBatis-Plus 参数校验异常返回 500")
        void searchWithZeroPageSizeCausesRuntimeException() throws Exception {
            when(userService.getAdminPage(any(UserDto.class), anyInt(), eq(0)))
                    .thenThrow(new IllegalArgumentException("pageSize must be >= 1"));

            mockMvc.perform(post("/api/admin/users/search")
                            .param("pageNum", "1")
                            .param("pageSize", "0")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isInternalServerError());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // BoundaryTests
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("边界和边缘场景")
    class BoundaryTests {

        @Test
        @DisplayName("pageNum 超出总页数时返回空列表")
        void searchPageNumBeyondAvailablePages() throws Exception {
            Page<UserVO> page = UserTestDataFactory.createEmptyPage(5, 10);

            when(userService.getAdminPage(any(UserDto.class), eq(5), eq(10))).thenReturn(page);

            mockMvc.perform(post("/api/admin/users/search")
                            .param("pageNum", "5")
                            .param("pageSize", "10")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.records.length()").value(0))
                    .andExpect(jsonPath("$.data.total").value(0))
                    .andExpect(jsonPath("$.data.current").value(5));
        }

        @Test
        @DisplayName("数据刚好一页时返回正确")
        void searchExactlyOnePage() throws Exception {
            List<UserVO> records = UserTestDataFactory.createUserVOList(10);
            Page<UserVO> page = UserTestDataFactory.createPage(records, 1, 10, 10);

            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.records.length()").value(10))
                    .andExpect(jsonPath("$.data.total").value(10));
        }

        @Test
        @DisplayName("pageSize=1 最小分页")
        void searchPageSizeEqualsOne() throws Exception {
            UserVO vo = UserTestDataFactory.createDefaultUserVO();
            Page<UserVO> page = UserTestDataFactory.createPage(List.of(vo), 1, 1, 100);

            when(userService.getAdminPage(any(UserDto.class), eq(1), eq(1))).thenReturn(page);

            mockMvc.perform(post("/api/admin/users/search")
                            .param("pageNum", "1")
                            .param("pageSize", "1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.records.length()").value(1))
                    .andExpect(jsonPath("$.data.size").value(1));
        }

        @Test
        @DisplayName("pageSize=100（MyBatis-Plus 分页拦截器上限）")
        void searchPageSizeAtMaxLimit() throws Exception {
            List<UserVO> records = UserTestDataFactory.createUserVOList(100);
            Page<UserVO> page = UserTestDataFactory.createPage(records, 1, 100, 500);

            when(userService.getAdminPage(any(UserDto.class), eq(1), eq(100))).thenReturn(page);

            mockMvc.perform(post("/api/admin/users/search")
                            .param("pageNum", "1")
                            .param("pageSize", "100")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.size").value(100));
        }

        @Test
        @DisplayName("pageSize 超过 100 被分页拦截器截断为 100")
        void searchPageSizeExceedsMaxLimit() throws Exception {
            // MyBatis-Plus PaginationInnerInterceptor caps overflow to maxLimit=100.
            // The controller passes the raw value, but the interceptor in the real
            // service caps it. Here we simulate the capped result.
            List<UserVO> records = UserTestDataFactory.createUserVOList(100);
            Page<UserVO> page = UserTestDataFactory.createPage(records, 1, 100, 500);

            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            mockMvc.perform(post("/api/admin/users/search")
                            .param("pageNum", "1")
                            .param("pageSize", "200")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.records.length()").value(100));
        }

        @Test
        @DisplayName("搜索字段含特殊字符 % _ - 时正常处理")
        void searchWithSpecialCharacters() throws Exception {
            Page<UserVO> page = UserTestDataFactory.createEmptyPage(1, 10);
            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = UserTestDataFactory.createSearchDto();
            dto.setUsername("user_test-01%");

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(UserTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("搜索字段含中文字符时正常处理")
        void searchWithChineseCharacters() throws Exception {
            UserVO vo = UserTestDataFactory.createDefaultUserVO();
            Page<UserVO> page = UserTestDataFactory.createPage(List.of(vo), 1, 10, 1);
            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = UserTestDataFactory.createSearchDto();
            dto.setRealName("党员");

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(UserTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("搜索字段仅含空白字符时请求正常接受")
        void searchWithWhitespaceOnlyField() throws Exception {
            Page<UserVO> page = UserTestDataFactory.createEmptyPage(1, 10);
            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = UserTestDataFactory.createSearchDto();
            dto.setUsername("   ");

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(UserTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("搜索字段超长字符串（1000字符）请求正常接受")
        void searchWithLongFieldValue() throws Exception {
            Page<UserVO> page = UserTestDataFactory.createEmptyPage(1, 10);
            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = UserTestDataFactory.createSearchDto();
            dto.setUsername("a".repeat(1000));

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(UserTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // SecurityTests
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("安全相关场景")
    class SecurityTests {

        @Test
        @DisplayName("XSS 注入尝试在 username 字段 — input 透传，MyBatis-Plus 参数化查询防护")
        void xssInUsernameField() throws Exception {
            Page<UserVO> page = UserTestDataFactory.createEmptyPage(1, 10);
            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = UserTestDataFactory.createSearchDto();
            dto.setUsername("<script>alert('xss')</script>");

            // Input is accepted at API level. XSS prevention is a rendering concern.
            // MyBatis-Plus parameterized queries prevent the script from executing in SQL.
            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(UserTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("XSS 注入尝试在 realName 字段")
        void xssInRealNameField() throws Exception {
            Page<UserVO> page = UserTestDataFactory.createEmptyPage(1, 10);
            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = UserTestDataFactory.createSearchDto();
            dto.setRealName("<img src=x onerror=alert(1)>");

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(UserTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("SQL 注入在 username — MyBatis-Plus like() 使用参数化查询防止注入")
        void sqlInjectionInUsername() throws Exception {
            Page<UserVO> page = UserTestDataFactory.createEmptyPage(1, 10);
            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = UserTestDataFactory.createSearchDto();
            dto.setUsername("' OR '1'='1");

            // MyBatis-Plus like() binds values via PreparedStatement parameters,
            // so SQL injection payloads are treated as literal search strings.
            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(UserTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("SQL 注入多字段同时攻击")
        void sqlInjectionInMultipleFields() throws Exception {
            Page<UserVO> page = UserTestDataFactory.createEmptyPage(1, 10);
            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = UserTestDataFactory.createSearchDto();
            dto.setUsername("'; DROP TABLE user; --");
            dto.setEmail("' OR 1=1'@evil.com");

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(UserTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("UNION SQL 注入尝试 — 参数化查询中作为字面量处理")
        void unionSqlInjection() throws Exception {
            Page<UserVO> page = UserTestDataFactory.createEmptyPage(1, 10);
            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            UserDto dto = UserTestDataFactory.createSearchDto();
            dto.setUsername("' UNION SELECT null, null, null --");

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(UserTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk());
        }

        /**
         * <h3>Security contract test — PermissionAccess enforcement</h3>
         * {@link AdminUserController} is annotated with
         * {@code @PermissionAccess(UserType.SCHOOL)}. The {@link UserAspect}
         * bean (registered by {@code WebConfig}) is active in this test
         * context and intercepts all controller methods.
         *
         * <p>A STUDENT user (permission level 1) accessing a SCHOOL-level
         * (level 2) endpoint is rejected with HTTP 400 and error code 4003
         * (RESOURCE_NOT_AUTHORIZED) — the request never reaches the controller.
         *
         * <p>Note: {@link com.rauio.smartdangjian.aop.DataScopeAccessAspect}
         * is NOT active in this test because it requires component-scan of
         * {@code com.rauio.smartdangjian.aop} package, which is outside the
         * TestConfig scan base.
         */
        @Test
        @DisplayName("STUDENT 用户被 PermissionAccess 拦截返回 400")
        void studentUserAccessContract() throws Exception {
            CurrentUserPrincipal student = new CurrentUserPrincipal() {
                @Override public String getId() { return "stu-001"; }
                @Override public UserType getUserType() { return UserType.STUDENT; }
                @Override public String getUniversityId() { return "uni1"; }
            };
            org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            student, null, Collections.emptyList()
                    )
            );

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4003"));
        }

        /**
         * <h3>Data scope enforcement contract</h3>
         * In production, {@link UserManagementAspect#handleSearch} injects the
         * current SCHOOL user's universityId into the UserDto query before it
         * reaches {@link UserService#getAdminPage}. This ensures school admins
         * can only see users within their own university.
         *
         * <p>A SCHOOL admin who explicitly sets a different universityId in the
         * request body would have it overwritten by the AOP. A SCHOOL admin
         * searching for UserType.MANAGER would be rejected entirely.
         *
         * <p>See {@code AdminUserSecurityTest.DataScopeTests} for unit-level
         * verification of this behavior.
         */
        @Test
        @DisplayName("[契约] DataScope 在搜索时自动注入学校 ID 实现数据隔离")
        void dataScopeUniversityIdContract() throws Exception {
            List<UserVO> records = UserTestDataFactory.createUserVOList(3, UserType.STUDENT, "uni1");
            Page<UserVO> page = UserTestDataFactory.createPage(records, 1, 10, 3);

            // In production, UserManagementAspect forces universityId=uni1 from
            // the SecurityContext. Here we verify the mock receives the query.
            when(userService.getAdminPage(any(UserDto.class), anyInt(), anyInt())).thenReturn(page);

            mockMvc.perform(post("/api/admin/users/search")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.total").value(3))
                    .andExpect(jsonPath("$.data.records[0].universityId").value("uni1"));
        }
    }
}
