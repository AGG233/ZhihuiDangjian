package com.rauio.smartdangjian.utils.spec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTypeTest {

    @Test
    @DisplayName("UserType 包含 STUDENT、SCHOOL、MANAGER 三个枚举值")
    void enumValues() {
        assertThat(UserType.values())
                .containsExactly(UserType.STUDENT, UserType.SCHOOL, UserType.MANAGER);
    }

    @Test
    @DisplayName("STUDENT getType 返回 学生")
    void studentGetType() {
        assertThat(UserType.STUDENT.getType()).isEqualTo("学生");
    }

    @Test
    @DisplayName("SCHOOL getType 返回 学校")
    void schoolGetType() {
        assertThat(UserType.SCHOOL.getType()).isEqualTo("学校");
    }

    @Test
    @DisplayName("MANAGER getType 返回 管理员")
    void managerGetType() {
        assertThat(UserType.MANAGER.getType()).isEqualTo("管理员");
    }

    @Test
    @DisplayName("toString 返回与 getType 相同的值")
    void toStringReturnsType() {
        assertThat(UserType.STUDENT.toString()).isEqualTo("学生");
        assertThat(UserType.SCHOOL.toString()).isEqualTo("学校");
        assertThat(UserType.MANAGER.toString()).isEqualTo("管理员");
    }

    @Test
    @DisplayName("valueOf STUDENT 返回正确的枚举")
    void valueOfStudent() {
        assertThat(UserType.valueOf("STUDENT")).isEqualTo(UserType.STUDENT);
    }

    @Test
    @DisplayName("valueOf SCHOOL 返回正确的枚举")
    void valueOfSchool() {
        assertThat(UserType.valueOf("SCHOOL")).isEqualTo(UserType.SCHOOL);
    }

    @Test
    @DisplayName("valueOf MANAGER 返回正确的枚举")
    void valueOfManager() {
        assertThat(UserType.valueOf("MANAGER")).isEqualTo(UserType.MANAGER);
    }
}
