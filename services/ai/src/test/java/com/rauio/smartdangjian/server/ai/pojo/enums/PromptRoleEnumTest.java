package com.rauio.smartdangjian.server.ai.pojo.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * PromptRoleEnum 与数据库枚举值映射回归。
 *
 * <p>数据库 {@code ai_prompts.role} 存小写值，因此枚举通过 {@code @EnumValue} 声明映射；
 * 若映射值写错或缺失，MyBatis-Plus 会按枚举名（大写）读写，而 MySQL ENUM 比较不区分大小写，
 * 写入被存成小写、读取时 {@code valueOf("system")} 失败，导致查询接口 500。
 *
 * <p>注解本身是否生效由 CI 烟雾测试端到端覆盖：{@code ci-seed.sql} 预置一行
 * {@code ai_prompts} 数据，使 {@code GET /api/admin/ai/prompts} 真正触发枚举转换；
 * 此处只校验映射值本身。
 */
@DisplayName("AI 提示词角色枚举映射")
class PromptRoleEnumTest {

    @Test
    @DisplayName("SYSTEM 映射数据库小写值 system")
    void systemMapsToLowerCaseDbValue() {
        assertThat(PromptRoleEnum.SYSTEM.dbValue()).isEqualTo("system");
    }

    @Test
    @DisplayName("DEVELOPER 映射数据库小写值 developer")
    void developerMapsToLowerCaseDbValue() {
        assertThat(PromptRoleEnum.DEVELOPER.dbValue()).isEqualTo("developer");
    }

    @Test
    @DisplayName("所有角色的数据库映射值非空且互不重复")
    void dbValuesAreUniqueAndNotBlank() {
        Set<String> seen = new HashSet<>();
        for (PromptRoleEnum role : PromptRoleEnum.values()) {
            String value = role.dbValue();
            assertThat(value).as("%s 的数据库映射值不能为空", role).isNotBlank();
            assertThat(seen.add(value)).as("%s 的映射值 %s 与其他角色重复", role, value).isTrue();
        }
    }
}
