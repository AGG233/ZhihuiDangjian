package com.rauio.smartdangjian.server.ai.pojo.enums;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * PromptRoleEnum 与数据库枚举值映射回归。
 *
 * <p>数据库 {@code ai_prompts.role} 存小写值，因此必须通过 {@link EnumValue} 声明映射；
 * 若缺失，MyBatis-Plus 按枚举名（大写）读写，而 MySQL ENUM 比较不区分大小写，
 * 写入被存成小写、读取时 {@code valueOf("system")} 失败，导致查询接口 500。
 */
@DisplayName("AI 提示词角色枚举映射")
class PromptRoleEnumTest {

    private static String dbValueOf(PromptRoleEnum role) throws ReflectiveOperationException {
        Field field = PromptRoleEnum.class.getDeclaredField("value");
        assertThat(field.isAnnotationPresent(EnumValue.class))
                .as("value 字段必须标注 @EnumValue，否则 MyBatis-Plus 按枚举名读写")
                .isTrue();
        field.setAccessible(true);
        return (String) field.get(role);
    }

    @Test
    @DisplayName("SYSTEM 映射数据库小写值 system")
    void systemMapsToLowerCaseDbValue() throws ReflectiveOperationException {
        assertThat(dbValueOf(PromptRoleEnum.SYSTEM)).isEqualTo("system");
    }

    @Test
    @DisplayName("DEVELOPER 映射数据库小写值 developer")
    void developerMapsToLowerCaseDbValue() throws ReflectiveOperationException {
        assertThat(dbValueOf(PromptRoleEnum.DEVELOPER)).isEqualTo("developer");
    }

    @Test
    @DisplayName("所有角色的数据库映射值非空且互不重复")
    void dbValuesAreUniqueAndNotBlank() throws ReflectiveOperationException {
        Set<String> seen = new HashSet<>();
        for (PromptRoleEnum role : PromptRoleEnum.values()) {
            String value = dbValueOf(role);
            assertThat(value).as("%s 的数据库映射值不能为空", role).isNotBlank();
            assertThat(seen.add(value)).as("%s 的映射值 %s 与其他角色重复", role, value).isTrue();
        }
    }
}
