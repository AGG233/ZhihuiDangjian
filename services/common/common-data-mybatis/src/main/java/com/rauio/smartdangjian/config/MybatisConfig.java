package com.rauio.smartdangjian.config;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@AutoConfiguration
public class MybatisConfig implements MetaObjectHandler {

    private final Clock clock;

    public MybatisConfig(Clock clock) {
        this.clock = clock;
    }

    /**
     * 添加分页插件
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor innerInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        innerInterceptor.setMaxLimit(100L);
        interceptor.addInnerInterceptor(innerInterceptor);
        return interceptor;
    }

    /**
     * @param metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createdAt", this::now, LocalDateTime.class);
        this.strictInsertFill(metaObject, "updatedAt", this::now, LocalDateTime.class);
        this.strictInsertFill(
                metaObject, "sessionId", () -> UUID.randomUUID().toString().replace("-", ""), String.class);
    }

    /**
     * @param metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", this::now, LocalDateTime.class);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }
}
