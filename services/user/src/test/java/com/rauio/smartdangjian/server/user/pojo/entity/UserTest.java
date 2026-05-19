package com.rauio.smartdangjian.server.user.pojo.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.rauio.smartdangjian.server.user.utils.spec.AccountStatus;
import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;
import com.rauio.smartdangjian.utils.spec.UserType;

class UserTest {

    @Test
    @DisplayName("builder 构造 User 所有字段值正确")
    void builderCreatesUserCorrectly() {
        LocalDateTime now = LocalDateTime.of(2025, 1, 1, 12, 0);
        User user = User.builder()
                .id("1001")
                .universityId("univ-1")
                .username("testuser")
                .password("secret")
                .realName("张三")
                .idCard("110101199001011234")
                .partyMemberId("PM-001")
                .joinPartyDate(now.minusYears(3))
                .partyStatus(PartyStatus.FORMAL_MEMBER)
                .branchName("第一党支部")
                .userType(UserType.STUDENT)
                .status(AccountStatus.ACTIVE)
                .email("test@example.com")
                .phone("13800138000")
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(user.getId()).isEqualTo("1001");
        assertThat(user.getUniversityId()).isEqualTo("univ-1");
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getPassword()).isEqualTo("secret");
        assertThat(user.getRealName()).isEqualTo("张三");
        assertThat(user.getIdCard()).isEqualTo("110101199001011234");
        assertThat(user.getPartyMemberId()).isEqualTo("PM-001");
        assertThat(user.getJoinPartyDate()).isEqualTo(now.minusYears(3));
        assertThat(user.getPartyStatus()).isEqualTo(PartyStatus.FORMAL_MEMBER);
        assertThat(user.getBranchName()).isEqualTo("第一党支部");
        assertThat(user.getUserType()).isEqualTo(UserType.STUDENT);
        assertThat(user.getStatus()).isEqualTo(AccountStatus.ACTIVE);
        assertThat(user.getEmail()).isEqualTo("test@example.com");
        assertThat(user.getPhone()).isEqualTo("13800138000");
        assertThat(user.getCreatedAt()).isEqualTo(now);
        assertThat(user.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("getAuthorities 用户类型为 STUDENT 时返回 ROLE_STUDENT")
    void getAuthoritiesReturnsRoleStudent() {
        User user = User.builder()
                .id("1001")
                .username("student1")
                .userType(UserType.STUDENT)
                .build();

        assertThat(user.getAuthorities()).singleElement().isEqualTo(new SimpleGrantedAuthority("ROLE_STUDENT"));
    }

    @Test
    @DisplayName("getAuthorities 用户类型为 SCHOOL 时返回 ROLE_SCHOOL")
    void getAuthoritiesReturnsRoleSchool() {
        User user = User.builder()
                .id("1002")
                .username("school1")
                .userType(UserType.SCHOOL)
                .build();

        assertThat(user.getAuthorities()).singleElement().isEqualTo(new SimpleGrantedAuthority("ROLE_SCHOOL"));
    }

    @Test
    @DisplayName("getAuthorities 用户类型为 MANAGER 时返回 ROLE_MANAGER")
    void getAuthoritiesReturnsRoleManager() {
        User user = User.builder()
                .id("1003")
                .username("admin1")
                .userType(UserType.MANAGER)
                .build();

        assertThat(user.getAuthorities()).singleElement().isEqualTo(new SimpleGrantedAuthority("ROLE_MANAGER"));
    }

    @Test
    @DisplayName("UserDetails isEnabled 默认返回 true")
    void isEnabledReturnsTrue() {
        User user = User.builder().id("1001").userType(UserType.STUDENT).build();

        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    @DisplayName("UserDetails isAccountNonExpired 默认返回 true")
    void isAccountNonExpiredReturnsTrue() {
        User user = User.builder().id("1001").userType(UserType.STUDENT).build();

        assertThat(user.isAccountNonExpired()).isTrue();
    }

    @Test
    @DisplayName("UserDetails isAccountNonLocked 默认返回 true")
    void isAccountNonLockedReturnsTrue() {
        User user = User.builder().id("1001").userType(UserType.STUDENT).build();

        assertThat(user.isAccountNonLocked()).isTrue();
    }

    @Test
    @DisplayName("UserDetails isCredentialsNonExpired 默认返回 true")
    void isCredentialsNonExpiredReturnsTrue() {
        User user = User.builder().id("1001").userType(UserType.STUDENT).build();

        assertThat(user.isCredentialsNonExpired()).isTrue();
    }

    @Test
    @DisplayName("CurrentUserPrincipal getId 返回 id 字段值")
    void getPrincipalIdReturnsId() {
        User user = User.builder().id("u123").userType(UserType.STUDENT).build();

        assertThat(user.getId()).isEqualTo("u123");
    }

    @Test
    @DisplayName("CurrentUserPrincipal getUserType 返回 userType 字段值")
    void getPrincipalUserTypeReturnsUserType() {
        User user = User.builder().id("u123").userType(UserType.MANAGER).build();

        assertThat(user.getUserType()).isEqualTo(UserType.MANAGER);
    }

    @Test
    @DisplayName("CurrentUserPrincipal getUniversityId 返回 universityId 字段值")
    void getPrincipalUniversityIdReturnsUniversityId() {
        User user = User.builder()
                .id("u123")
                .universityId("univ-1")
                .userType(UserType.SCHOOL)
                .build();

        assertThat(user.getUniversityId()).isEqualTo("univ-1");
    }

    @Test
    @DisplayName("全参构造器创建 User 所有字段正确")
    void allArgsConstructorWorks() {
        LocalDateTime now = LocalDateTime.of(2025, 6, 1, 10, 0);
        User user = new User(
                "u1",
                "univ-1",
                "testuser",
                "pass",
                "张三",
                "idcard",
                "pm-1",
                now.minusYears(2),
                PartyStatus.FORMAL_MEMBER,
                "支部",
                UserType.STUDENT,
                AccountStatus.ACTIVE,
                "test@test.com",
                "138",
                now,
                now);

        assertThat(user.getId()).isEqualTo("u1");
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getUserType()).isEqualTo(UserType.STUDENT);
        assertThat(user.getStatus()).isEqualTo(AccountStatus.ACTIVE);
    }

    @Test
    @DisplayName("setter 修改 username 字段后 getter 返回新值")
    void setterAndGetterWork() {
        User user = User.builder()
                .id("u1")
                .username("oldname")
                .userType(UserType.STUDENT)
                .build();

        user.setUsername("newname");

        assertThat(user.getUsername()).isEqualTo("newname");
    }
}
