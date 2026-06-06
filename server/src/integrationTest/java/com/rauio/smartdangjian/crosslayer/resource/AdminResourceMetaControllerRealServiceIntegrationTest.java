package com.rauio.smartdangjian.crosslayer.resource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.rauio.smartdangjian.crosslayer.CrossLayerTestBase;
import com.rauio.smartdangjian.server.resource.constants.ResourceStatusConstants;
import com.rauio.smartdangjian.server.resource.controller.admin.AdminResourceMetaController;
import com.rauio.smartdangjian.server.resource.mapper.ResourceMetaMapper;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.service.ResourceMetaService;
import com.rauio.smartdangjian.service.PermissionValidator;

@SpringBootTest(classes = AdminResourceMetaControllerRealServiceIntegrationTest.TestConfig.class)
@DisplayName("管理员资源元数据控制层真实 ResourceMetaService 集成测试")
class AdminResourceMetaControllerRealServiceIntegrationTest extends CrossLayerTestBase {

    @Autowired
    private ResourceMetaMapper resourceMetaMapper;

    @Autowired
    private PermissionValidator permissionValidator;

    @BeforeEach
    void resetMocks() {
        reset(resourceMetaMapper, permissionValidator);
    }

    @Test
    @DisplayName("POST /files 使用真实 ResourceMetaService 创建资源元数据并默认公开状态")
    void createUsesRealResourceMetaServiceAndDefaultsPublicStatus() throws Exception {
        when(resourceMetaMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(resourceMetaMapper.insert(any(ResourceMeta.class))).thenReturn(1);

        mockMvc.perform(
                        post("/api/admin/resource/files")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"uploaderId":"7","originalName":"党课封面.png","hash":"hash-1","objectKey":"image/hash-1.png","resourceType":0}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data.uploaderId").value(7))
                .andExpect(jsonPath("$.data.originalName").value("党课封面.png"))
                .andExpect(jsonPath("$.data.status").value(ResourceStatusConstants.PUBLIC));

        ArgumentCaptor<ResourceMeta> captor = ArgumentCaptor.forClass(ResourceMeta.class);
        verify(resourceMetaMapper).insert(captor.capture());
        assertThat(captor.getValue().getUploaderId()).isEqualTo(7L);
        assertThat(captor.getValue().getHash()).isEqualTo("hash-1");
        assertThat(captor.getValue().getStatus()).isEqualTo(ResourceStatusConstants.PUBLIC);
    }

    @Test
    @DisplayName("POST /files 字段校验失败时不进入真实 ResourceMetaService 依赖")
    void createValidationFailureStopsBeforeMapperAccess() throws Exception {
        mockMvc.perform(
                        post("/api/admin/resource/files")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"uploaderId":"","originalName":"党课封面.png","hash":"hash-1","objectKey":"image/hash-1.png","resourceType":0}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("uploaderId不能为空"));

        verify(resourceMetaMapper, never()).selectOne(any(Wrapper.class));
        verify(resourceMetaMapper, never()).insert(any(ResourceMeta.class));
    }

    @Test
    @DisplayName("PUT /files/{id} 使用真实 ResourceMetaService 合并更新可编辑字段")
    void updateUsesRealResourceMetaServiceAndMergesEditableFields() throws Exception {
        ResourceMeta existing = ResourceMeta.builder()
                .id(100L)
                .uploaderId(7L)
                .originalName("old.png")
                .hash("hash-1")
                .objectKey("image/old.png")
                .resourceType(0)
                .status(ResourceStatusConstants.PUBLIC)
                .build();
        when(resourceMetaMapper.selectById(100L)).thenReturn(existing);
        when(resourceMetaMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(resourceMetaMapper.updateById(any(ResourceMeta.class))).thenReturn(1);

        mockMvc.perform(
                        put("/api/admin/resource/files/100")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                {"originalName":"new.png","resourceType":1,"status":2}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));

        verify(permissionValidator).requireResourceAccess(7L);
        ArgumentCaptor<ResourceMeta> captor = ArgumentCaptor.forClass(ResourceMeta.class);
        verify(resourceMetaMapper).updateById(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(100L);
        assertThat(captor.getValue().getOriginalName()).isEqualTo("new.png");
        assertThat(captor.getValue().getObjectKey()).isEqualTo("image/old.png");
        assertThat(captor.getValue().getResourceType()).isEqualTo(1);
        assertThat(captor.getValue().getStatus()).isEqualTo(ResourceStatusConstants.HIDDEN);
    }

    @Test
    @DisplayName("PUT /files/{id} 空白 originalName 被字段约束拒绝")
    void updateRejectsBlankEditableTextFields() throws Exception {
        mockMvc.perform(put("/api/admin/resource/files/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"originalName\":\"   \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("originalName不能为空白字符"));

        verify(resourceMetaMapper, never()).selectById(any());
        verify(permissionValidator, never()).requireResourceAccess(any());
    }

    @Test
    @DisplayName("DELETE /files/{id} 使用真实 ResourceMetaService 校验权限后删除")
    void deleteUsesRealResourceMetaServiceAndChecksResourceAccess() throws Exception {
        ResourceMeta existing = ResourceMeta.builder()
                .id(100L)
                .uploaderId(7L)
                .hash("hash-1")
                .objectKey("image/hash-1.png")
                .build();
        when(resourceMetaMapper.selectById(100L)).thenReturn(existing);
        when(resourceMetaMapper.deleteById(100L)).thenReturn(1);

        mockMvc.perform(delete("/api/admin/resource/files/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data").value(true));

        verify(permissionValidator).requireResourceAccess(7L);
        verify(resourceMetaMapper).deleteById(100L);
    }

    @Test
    @DisplayName("GET /files 使用真实 ResourceMetaService 构造筛选查询")
    void listUsesRealResourceMetaServiceFilters() throws Exception {
        when(resourceMetaMapper.selectList(any(Wrapper.class)))
                .thenReturn(java.util.List.of(ResourceMeta.builder()
                        .id(100L)
                        .uploaderId(7L)
                        .originalName("党课封面.png")
                        .hash("hash-1")
                        .resourceType(0)
                        .status(ResourceStatusConstants.PUBLIC)
                        .build()));

        mockMvc.perform(get("/api/admin/resource/files")
                        .param("uploaderId", "7")
                        .param("originalName", "党课")
                        .param("hash", "hash-1")
                        .param("resourceType", "0")
                        .param("status", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("200"))
                .andExpect(jsonPath("$.data[0].id").value(100));

        verify(resourceMetaMapper).selectList(any(Wrapper.class));
    }

    @SpringBootConfiguration
    static class TestConfig extends CrossLayerTestConfig {

        @Bean
        ResourceMetaMapper resourceMetaMapper() {
            return mock(ResourceMetaMapper.class);
        }

        @Bean
        PermissionValidator permissionValidator() {
            return mock(PermissionValidator.class);
        }

        @Bean
        ResourceMetaService resourceMetaService(
                ResourceMetaMapper resourceMetaMapper, PermissionValidator permissionValidator) {
            ResourceMetaService service = new ResourceMetaService(permissionValidator);
            try {
                org.springframework.test.util.ReflectionTestUtils.setField(service, "baseMapper", resourceMetaMapper);
            } catch (Exception e) {
                throw new RuntimeException("Failed to set baseMapper on ResourceMetaService", e);
            }
            return service;
        }

        @Bean
        AdminResourceMetaController adminResourceMetaController(ResourceMetaService resourceMetaService) {
            return new AdminResourceMetaController(resourceMetaService);
        }

        @Bean
        AbstractPlatformTransactionManager transactionManager() {
            return new AbstractPlatformTransactionManager() {
                @Override
                protected Object doGetTransaction() {
                    return new Object();
                }

                @Override
                protected void doBegin(Object transaction, TransactionDefinition definition) {
                    // no-op
                }

                @Override
                protected void doCommit(DefaultTransactionStatus status) {
                    // no-op
                }

                @Override
                protected void doRollback(DefaultTransactionStatus status) {
                    // no-op
                }
            };
        }
    }
}
