package com.rauio.smartdangjian.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

class MybatisConfigTest {

    private final MybatisConfig config = new MybatisConfig();

    @BeforeAll
    static void initTableInfo() {
        MybatisConfiguration configuration = new MybatisConfiguration();
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(configuration, "");
        TableInfoHelper.initTableInfo(assistant, FillEntity.class);
    }

    @Test
    @DisplayName("mybatisPlusInterceptor 创建分页拦截器")
    void mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = config.mybatisPlusInterceptor();

        assertThat(interceptor).isNotNull();
    }

    @Test
    @DisplayName("mybatisPlusInterceptor 分页上限为 100")
    void paginationMaxLimitIs100() {
        MybatisPlusInterceptor interceptor = config.mybatisPlusInterceptor();

        assertThat(interceptor.getInterceptors()).hasSize(1);
        PaginationInnerInterceptor inner =
                (PaginationInnerInterceptor) interceptor.getInterceptors().get(0);
        assertThat(inner.getMaxLimit()).isEqualTo(100L);
    }

    @Test
    @DisplayName("insertFill 调用 strictInsertFill 填充自动填充字段")
    void insertFill() {
        var pojo = new FillEntity();
        var metaObject = MetaObject.forObject(
                pojo, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());
        config.insertFill(metaObject);

        assertThat(pojo.getCreatedAt()).isNotNull();
        assertThat(pojo.getUpdatedAt()).isNotNull();
        assertThat(pojo.getSessionId()).isNotNull();
    }

    @Test
    @DisplayName("updateFill 调用 strictUpdateFill 填充更新时间")
    void updateFill() {
        var pojo = new FillEntity();
        pojo.setCreatedAt(LocalDateTime.now());
        var metaObject = MetaObject.forObject(
                pojo, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(), new DefaultReflectorFactory());
        config.updateFill(metaObject);

        assertThat(pojo.getUpdatedAt()).isNotNull();
    }

    @TableName("test_fill")
    public static class FillEntity {
        @TableField(fill = FieldFill.INSERT)
        private LocalDateTime createdAt;

        @TableField(fill = FieldFill.INSERT_UPDATE)
        private LocalDateTime updatedAt;

        @TableField(fill = FieldFill.INSERT)
        private String sessionId;

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public void setCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
        }

        public LocalDateTime getUpdatedAt() {
            return updatedAt;
        }

        public void setUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
        }

        public String getSessionId() {
            return sessionId;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }
    }
}
