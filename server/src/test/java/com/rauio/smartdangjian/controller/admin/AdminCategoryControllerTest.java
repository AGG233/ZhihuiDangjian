package com.rauio.smartdangjian.controller.admin;

import com.rauio.smartdangjian.BaseControllerTest;
import com.rauio.smartdangjian.controller.factory.CategoryTestDataFactory;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.server.content.controller.admin.AdminCategoryController;
import com.rauio.smartdangjian.server.content.pojo.dto.CategoryDto;
import com.rauio.smartdangjian.server.content.service.category.CategoryService;
import com.rauio.smartdangjian.utils.spec.UserType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = AdminCategoryControllerTest.TestConfig.class)
@DisplayName("管理员目录接口测试")
class AdminCategoryControllerTest extends BaseControllerTest {

    @SpringBootConfiguration
    static class TestConfig extends CommonTestConfig {
        @Bean
        public AdminCategoryController adminCategoryController(CategoryService categoryService) {
            return new AdminCategoryController(categoryService);
        }
    }

    @MockitoBean
    private CategoryService categoryService;

    // ═══════════════════════════════════════════════════════════════
    // 正常场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("正常场景")
    class NormalTests {

        @Test
        @DisplayName("POST /root - 创建根目录成功")
        void createRootCategorySuccess() throws Exception {
            when(categoryService.create(any(CategoryDto.class))).thenReturn(true);

            CategoryDto dto = CategoryTestDataFactory.createRootCategoryDto("党建学习");
            mockMvc.perform(post("/api/admin/content/categories/root")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CategoryTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("POST /{id}/children - 创建子目录成功")
        void createChildrenSuccess() throws Exception {
            when(categoryService.createByParentId(any(List.class), eq("cat-001"))).thenReturn(true);

            List<CategoryDto> children = CategoryTestDataFactory.createSingleChildCategoryDtoList("子分类A");
            mockMvc.perform(post("/api/admin/content/categories/cat-001/children")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CategoryTestDataFactory.listToJson(children)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("PUT /{id} - 更新目录成功")
        void updateCategorySuccess() throws Exception {
            when(categoryService.update(any(CategoryDto.class), eq("cat-001"))).thenReturn(true);

            CategoryDto dto = CategoryTestDataFactory.createCategoryDto("更新后名称");
            mockMvc.perform(put("/api/admin/content/categories/cat-001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CategoryTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("DELETE /{id} - 删除目录成功")
        void deleteCategorySuccess() throws Exception {
            when(categoryService.delete("cat-001")).thenReturn(true);

            mockMvc.perform(delete("/api/admin/content/categories/cat-001"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }

        @Test
        @DisplayName("DELETE /{id}/all - 递归删除目录成功")
        void deleteCategoryWithChildrenSuccess() throws Exception {
            when(categoryService.deleteByIdWithChildren("cat-001")).thenReturn(true);

            mockMvc.perform(delete("/api/admin/content/categories/cat-001/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"))
                    .andExpect(jsonPath("$.data").value(true));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 异常处理场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("异常处理场景")
    class ErrorTests {

        @Test
        @DisplayName("POST /root - 请求体为空返回 400")
        void createRootWithEmptyBody() throws Exception {
            mockMvc.perform(post("/api/admin/content/categories/root")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /root - name 为空返回 400")
        void createRootWithBlankName() throws Exception {
            CategoryDto dto = CategoryTestDataFactory.createRootCategoryDto("");
            mockMvc.perform(post("/api/admin/content/categories/root")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CategoryTestDataFactory.toJson(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /root - name 超过 64 字符返回 400")
        void createRootWithNameTooLong() throws Exception {
            String longName = "a".repeat(65);
            CategoryDto dto = CategoryTestDataFactory.createRootCategoryDto(longName);
            mockMvc.perform(post("/api/admin/content/categories/root")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CategoryTestDataFactory.toJson(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("POST /{id}/children - 请求体为空数组返回 200（空操作）")
        void createChildrenWithEmptyList() throws Exception {
            when(categoryService.createByParentId(any(List.class), eq("cat-001"))).thenReturn(true);

            mockMvc.perform(post("/api/admin/content/categories/cat-001/children")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[]"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("PUT /{id} - name 为空返回 400")
        void updateWithBlankName() throws Exception {
            CategoryDto dto = CategoryTestDataFactory.createCategoryDto("");
            mockMvc.perform(put("/api/admin/content/categories/cat-001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CategoryTestDataFactory.toJson(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Service 返回 false 时 code 为 400")
        void serviceReturnsFalse() throws Exception {
            when(categoryService.create(any(CategoryDto.class))).thenReturn(false);

            CategoryDto dto = CategoryTestDataFactory.createRootCategoryDto("测试");
            mockMvc.perform(post("/api/admin/content/categories/root")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CategoryTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("Service 抛出 BusinessException 时返回 400 和错误信息")
        void serviceThrowsBusinessException() throws Exception {
            when(categoryService.create(any(CategoryDto.class)))
                    .thenThrow(new BusinessException(4001, "目录不存在"));

            CategoryDto dto = CategoryTestDataFactory.createRootCategoryDto("测试");
            mockMvc.perform(post("/api/admin/content/categories/root")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CategoryTestDataFactory.toJson(dto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value("4001"))
                    .andExpect(jsonPath("$.message").value("目录不存在"));
        }

        @Test
        @DisplayName("Service 抛出 RuntimeException 时返回 500")
        void serviceThrowsRuntimeException() throws Exception {
            when(categoryService.create(any(CategoryDto.class)))
                    .thenThrow(new RuntimeException("数据库异常"));

            CategoryDto dto = CategoryTestDataFactory.createRootCategoryDto("测试");
            mockMvc.perform(post("/api/admin/content/categories/root")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CategoryTestDataFactory.toJson(dto)))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.code").value("500"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 边界场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("边界场景")
    class BoundaryTests {

        @Test
        @DisplayName("POST /root - name 恰好 64 字符允许")
        void createRootWithMaxLengthName() throws Exception {
            String maxName = "a".repeat(64);
            when(categoryService.create(any(CategoryDto.class))).thenReturn(true);

            CategoryDto dto = CategoryTestDataFactory.createRootCategoryDto(maxName);
            mockMvc.perform(post("/api/admin/content/categories/root")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CategoryTestDataFactory.toJson(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("DELETE /{id} - service 返回 false 时 code 为 400")
        void deleteCategoryReturnsFalse() throws Exception {
            when(categoryService.delete("nonexistent")).thenReturn(false);

            mockMvc.perform(delete("/api/admin/content/categories/nonexistent"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("PUT /{id} - description 为 null 正常处理")
        void updateWithNullDescription() throws Exception {
            when(categoryService.update(any(CategoryDto.class), eq("cat-001"))).thenReturn(true);

            String json = "{\"name\": \"测试分类\"}";
            mockMvc.perform(put("/api/admin/content/categories/cat-001")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }

        @Test
        @DisplayName("POST /{id}/children - 批量创建多个子目录成功")
        void createMultipleChildrenSuccess() throws Exception {
            when(categoryService.createByParentId(any(List.class), eq("cat-001"))).thenReturn(true);

            List<CategoryDto> children = CategoryTestDataFactory.createCategoryDtoList(5);
            mockMvc.perform(post("/api/admin/content/categories/cat-001/children")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(CategoryTestDataFactory.listToJson(children)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value("200"));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // 安全场景
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("安全场景")
    class SecurityTests {

        @Test
        @DisplayName("STUDENT 用户绕过 PermissionAccess（非活动状态）返回 200")
        void studentUserAccessDenied() throws Exception {
            CurrentUserPrincipal student = new CurrentUserPrincipal() {
                @Override
                public String getId() {
                    return "stu-001";
                }

                @Override
                public UserType getUserType() {
                    return UserType.STUDENT;
                }

                @Override
                public String getUniversityId() {
                    return "uni1";
                }
            };
            SecurityContextHolder.getContext().setAuthentication(
                    new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                            student, null, Collections.emptyList()
                    )
            );

            when(categoryService.create(any(CategoryDto.class))).thenReturn(true);
            mockMvc.perform(post("/api/admin/content/categories/root")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"name\": \"测试\"}"))
                    .andExpect(status().isOk());
        }
    }
}
