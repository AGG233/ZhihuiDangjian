package com.rauio.smartdangjian.server.ai.pojo.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * AI 提示词角色。
 *
 * <p>数据库 {@code ai_prompts.role} 枚举存小写值（{@code system}/{@code developer}），
 * 因此必须用 {@link EnumValue} 显式声明映射值：MyBatis-Plus 默认按枚举名（大写）读写，
 * 而 MySQL 的 ENUM 比较不区分大小写，写入会被存成小写，读取时
 * {@code valueOf("system")} 又大小写敏感，导致查询抛出
 * {@code IllegalArgumentException: No enum constant ...PromptRoleEnum.system}。
 *
 * <p>不加 {@code @JsonValue}：API 响应的 role 字段保持输出枚举名（{@code SYSTEM}），
 * 与前端既有契约一致；请求侧由 {@code PromptService#parsePromptRole} 兼容大小写。
 */
@Schema(description = "AI提示词角色")
public enum PromptRoleEnum {
    @Schema(description = "系统角色")
    SYSTEM("system"),
    @Schema(description = "开发者角色")
    DEVELOPER("developer");

    @EnumValue
    private final String value;

    PromptRoleEnum(String value) {
        this.value = value;
    }

    /**
     * 该角色在数据库 {@code ai_prompts.role} 中的枚举值。
     *
     * <p>刻意不命名为 {@code getValue()}：Jackson 会把 {@code getXxx()} 识别为属性，
     * 从而改变 API 中 role 字段的序列化形式（当前输出枚举名 {@code SYSTEM}）。
     *
     * @return 数据库存储值（小写）
     */
    public String dbValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
