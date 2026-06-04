package com.rauio.smartdangjian.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

/**
 * 文件存储相关配置属性。
 *
 * <p>注意：{@code services/resource} 模块中的 {@code FileService} 也通过 {@code @Value} 读取同一属性
 * ({@code app.storage.local-root})，后续应统一迁移至此配置类。</p>
 */
@Data
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    /** 文件存储本地根目录 */
    private String localRoot = "./uploads";
}
