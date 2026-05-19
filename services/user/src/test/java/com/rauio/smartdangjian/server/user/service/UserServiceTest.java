package com.rauio.smartdangjian.server.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.user.constants.UserErrorConstants;
import com.rauio.smartdangjian.server.user.pojo.convertor.UserConvertor;
import com.rauio.smartdangjian.server.user.pojo.dto.UserDto;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.pojo.vo.UserPublicVO;
import com.rauio.smartdangjian.server.user.pojo.vo.UserVO;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserConvertor convertor;

    @Spy
    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userService, "defaultDevUserId", "default-dev-id");
    }

    // ---------- helpers ----------

    private User createUser(String id, String username, String email, String phone) {
        return User.builder()
                .id(id)
                .username(username)
                .email(email)
                .phone(phone)
                .password("encodedOldPassword")
                .universityId("univ-1")
                .partyMemberId("pm-1")
                .build();
    }

    // ================================================================
    // getByPassport
    // ================================================================

    @Test
    @DisplayName("getByPassport passport为null时返回null")
    void getByPassportNullReturnsNull() {
        User result = userService.getByPassport(null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getByPassport passport为空字符串时返回null")
    void getByPassportEmptyReturnsNull() {
        User result = userService.getByPassport("");
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("getByPassport passport包含@时调用getByEmail并返回结果")
    void getByPassportWithAtDelegatesToEmail() {
        User expectedUser = createUser("u1", "testuser", "test@example.com", "13800138000");
        doReturn(expectedUser).when(userService).getByEmail("test@example.com");

        User result = userService.getByPassport("test@example.com");

        assertThat(result).isEqualTo(expectedUser);
    }

    @Test
    @DisplayName("getByPassport passport包含+时调用getByPhone并返回结果")
    void getByPassportWithPlusDelegatesToPhone() {
        User expectedUser = createUser("u1", "testuser", "test@example.com", "+8613800138000");
        doReturn(expectedUser).when(userService).getByPhone("+8613800138000");

        User result = userService.getByPassport("+8613800138000");

        assertThat(result).isEqualTo(expectedUser);
    }

    @Test
    @DisplayName("getByPassport passport为普通字符串时调用getByUsername并返回结果")
    void getByPassportPlainDelegatesToUsername() {
        User expectedUser = createUser("u1", "testuser", "test@example.com", "13800138000");
        doReturn(expectedUser).when(userService).getByUsername("testuser");

        User result = userService.getByPassport("testuser");

        assertThat(result).isEqualTo(expectedUser);
    }

    // ================================================================
    // get
    // ================================================================

    @Test
    @DisplayName("get 根据ID调用getById并转换为UserVO返回")
    void getByIdConvertsToVO() {
        User user = createUser("u1", "testuser", "test@example.com", "13800138000");
        UserVO expectedVO = new UserVO();
        expectedVO.setId("u1");
        expectedVO.setUsername("testuser");

        doReturn(user).when(userService).getById("u1");
        when(convertor.toVO(user)).thenReturn(expectedVO);

        UserVO result = userService.get("u1");

        assertThat(result).isEqualTo(expectedVO);
        verify(convertor).toVO(user);
    }

    // ================================================================
    // getCurrentUser
    // ================================================================

    @Test
    @DisplayName("getCurrentUser 认证成功且principal为User时返回该User")
    void getCurrentUserAuthenticatedReturnsUser() {
        User user = createUser("u1", "testuser", "test@example.com", "13800138000");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);

            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn(user);

            User result = userService.getCurrentUser();

            assertThat(result).isEqualTo(user);
        }
    }

    @Test
    @DisplayName("getCurrentUser principal不是User实例时返回null")
    void getCurrentUserPrincipalNotUserReturnsNull() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);

            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn("not-a-user-instance");

            User result = userService.getCurrentUser();

            assertThat(result).isNull();
        }
    }

    @Test
    @DisplayName("getCurrentUser authentication为null时返回null")
    void getCurrentUserNullAuthenticationReturnsNull() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);

            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            User result = userService.getCurrentUser();

            assertThat(result).isNull();
        }
    }

    // ================================================================
    // getCurrentUserId
    // ================================================================

    @Test
    @DisplayName("getCurrentUserId 认证成功时返回用户ID")
    void getCurrentUserIdAuthenticatedReturnsId() {
        User user = createUser("user-id-123", "testuser", "test@example.com", "13800138000");

        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);
            Authentication authentication = mock(Authentication.class);

            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(authentication);
            when(authentication.isAuthenticated()).thenReturn(true);
            when(authentication.getPrincipal()).thenReturn(user);

            String result = userService.getCurrentUserId();

            assertThat(result).isEqualTo("user-id-123");
        }
    }

    @Test
    @DisplayName("getCurrentUserId 未认证时返回默认开发用户ID")
    void getCurrentUserIdNotAuthenticatedReturnsDefaultId() {
        try (MockedStatic<SecurityContextHolder> mockedStatic = mockStatic(SecurityContextHolder.class)) {
            SecurityContext securityContext = mock(SecurityContext.class);

            mockedStatic.when(SecurityContextHolder::getContext).thenReturn(securityContext);
            when(securityContext.getAuthentication()).thenReturn(null);

            String result = userService.getCurrentUserId();

            assertThat(result).isEqualTo("default-dev-id");
        }
    }

    // ================================================================
    // getByUsername
    // ================================================================

    @Test
    @DisplayName("getByUsername 调用getOne查询并返回用户")
    void getByUsernameReturnsUser() {
        User expectedUser = createUser("u1", "testuser", "test@example.com", "13800138000");
        doReturn(expectedUser).when(userService).getOne(any(LambdaQueryWrapper.class));

        User result = userService.getByUsername("testuser");

        assertThat(result).isEqualTo(expectedUser);
    }

    // ================================================================
    // getByEmail
    // ================================================================

    @Test
    @DisplayName("getByEmail 调用getOne查询并返回用户")
    void getByEmailReturnsUser() {
        User expectedUser = createUser("u1", "testuser", "test@example.com", "13800138000");
        doReturn(expectedUser).when(userService).getOne(any(LambdaQueryWrapper.class));

        User result = userService.getByEmail("test@example.com");

        assertThat(result).isEqualTo(expectedUser);
    }

    // ================================================================
    // getByPhone
    // ================================================================

    @Test
    @DisplayName("getByPhone 调用getOne查询并返回用户")
    void getByPhoneReturnsUser() {
        User expectedUser = createUser("u1", "testuser", "test@example.com", "13800138000");
        doReturn(expectedUser).when(userService).getOne(any(LambdaQueryWrapper.class));

        User result = userService.getByPhone("13800138000");

        assertThat(result).isEqualTo(expectedUser);
    }

    // ================================================================
    // getByPartyMemberId
    // ================================================================

    @Test
    @DisplayName("getByPartyMemberId 调用getOne查询并返回用户")
    void getByPartyMemberIdReturnsUser() {
        User expectedUser = createUser("u1", "testuser", "test@example.com", "13800138000");
        doReturn(expectedUser).when(userService).getOne(any(LambdaQueryWrapper.class));

        User result = userService.getByPartyMemberId("pm-1");

        assertThat(result).isEqualTo(expectedUser);
    }

    // ================================================================
    // update
    // ================================================================

    @Test
    @DisplayName("update 无密码时仅设置ID并更新")
    void updateWithoutPassword() {
        User user = createUser(null, "testuser", "test@example.com", "13800138000");
        user.setPassword(null);

        doReturn(true).when(userService).updateById(any(User.class));

        Boolean result = userService.update("u1", user);

        assertThat(result).isTrue();
        assertThat(user.getId()).isEqualTo("u1");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userService).updateById(user);
    }

    @Test
    @DisplayName("update 有密码时加密后更新")
    void updateWithPasswordEncodesAndUpdates() {
        User user = createUser(null, "testuser", "test@example.com", "13800138000");
        user.setPassword("plainPassword");

        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedNewPassword");
        doReturn(true).when(userService).updateById(any(User.class));

        Boolean result = userService.update("u1", user);

        assertThat(result).isTrue();
        assertThat(user.getId()).isEqualTo("u1");
        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
        verify(passwordEncoder).encode("plainPassword");
        verify(userService).updateById(user);
    }

    @Test
    @DisplayName("update 密码为空字符串时不加密")
    void updateWithEmptyPasswordDoesNotEncode() {
        User user = createUser(null, "testuser", "test@example.com", "13800138000");
        user.setPassword("");

        doReturn(true).when(userService).updateById(any(User.class));

        Boolean result = userService.update("u1", user);

        assertThat(result).isTrue();
        verify(passwordEncoder, never()).encode(anyString());
    }

    // ================================================================
    // delete
    // ================================================================

    @Test
    @DisplayName("delete 调用removeById删除并返回结果")
    void deleteCallsRemoveById() {
        doReturn(true).when(userService).removeById("u1");

        Boolean result = userService.delete("u1");

        assertThat(result).isTrue();
        verify(userService).removeById("u1");
    }

    @Test
    @DisplayName("delete 删除不存在的用户返回false")
    void deleteNonExistentReturnsFalse() {
        doReturn(false).when(userService).removeById("nonexistent");

        Boolean result = userService.delete("nonexistent");

        assertThat(result).isFalse();
    }

    // ================================================================
    // register
    // ================================================================

    @Test
    @DisplayName("register 所有校验通过后成功注册")
    void registerSuccessWhenAllChecksPass() {
        User user = createUser(null, "newuser", "new@example.com", "13900139000");
        user.setPassword("plainPassword");

        doReturn(false).when(userService).exists(any(LambdaQueryWrapper.class));
        when(passwordEncoder.encode("plainPassword")).thenReturn("encodedPassword");
        doReturn(true).when(userService).save(any(User.class));

        Boolean result = userService.register(user);

        assertThat(result).isTrue();
        assertThat(user.getPassword()).isEqualTo("encodedPassword");
        verify(passwordEncoder).encode("plainPassword");
        verify(userService).save(user);
    }

    @Test
    @DisplayName("register 邮箱已注册时抛出BusinessException(EMAIL_EXISTS)")
    void registerThrowsWhenEmailExists() {
        User user = createUser(null, "newuser", "dup@example.com", "13900139000");
        user.setPassword("plainPassword");

        doReturn(true).when(userService).exists(any(LambdaQueryWrapper.class));

        assertThatThrownBy(() -> userService.register(user))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(UserErrorConstants.EMAIL_EXISTS);
    }

    @Test
    @DisplayName("register 手机号已注册时抛出BusinessException(PHONE_EXISTS)")
    void registerThrowsWhenPhoneExists() {
        User user = createUser(null, "newuser", "new@example.com", "13900139000");
        user.setPassword("plainPassword");

        doReturn(false, true).when(userService).exists(any(LambdaQueryWrapper.class));

        assertThatThrownBy(() -> userService.register(user))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(UserErrorConstants.PHONE_EXISTS);
    }

    @Test
    @DisplayName("register 用户名已占用时抛出BusinessException(USERNAME_EXISTS)")
    void registerThrowsWhenUsernameOccupied() {
        User user = createUser(null, "dupuser", "new@example.com", "13900139000");
        user.setPassword("plainPassword");

        doReturn(false, false, true).when(userService).exists(any(LambdaQueryWrapper.class));

        assertThatThrownBy(() -> userService.register(user))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(UserErrorConstants.USERNAME_EXISTS);
    }

    @Test
    @DisplayName("register 党员编号已存在时抛出BusinessException(PARTY_MEMBER_ID_EXISTS)")
    void registerThrowsWhenPartyMemberIdExists() {
        User user = createUser(null, "newuser", "new@example.com", "13900139000");
        user.setPartyMemberId("dup-pm-id");
        user.setPassword("plainPassword");

        doReturn(false, false, false, true).when(userService).exists(any(LambdaQueryWrapper.class));

        assertThatThrownBy(() -> userService.register(user))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(UserErrorConstants.PARTY_MEMBER_ID_EXISTS);
    }

    // ================================================================
    // changePassword
    // ================================================================

    @Test
    @DisplayName("changePassword oldPassword为null时抛出BusinessException(EMPTY_ARGS)")
    void changePasswordNullOldPasswordThrows() {
        assertThatThrownBy(() -> userService.changePassword(null, "newPassword"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(UserErrorConstants.EMPTY_ARGS);
    }

    @Test
    @DisplayName("changePassword oldPassword为空字符串时抛出BusinessException(EMPTY_ARGS)")
    void changePasswordEmptyOldPasswordThrows() {
        assertThatThrownBy(() -> userService.changePassword("", "newPassword"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(UserErrorConstants.EMPTY_ARGS);
    }

    @Test
    @DisplayName("changePassword 密码匹配成功时加密新密码并更新")
    void changePasswordSuccessWhenMatch() {
        User user = createUser("u1", "testuser", "test@example.com", "13800138000");
        user.setPassword("encodedOldPassword");

        doReturn(user).when(userService).getCurrentUser();
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        doReturn(true).when(userService).updateById(any(User.class));

        Boolean result = userService.changePassword("oldPassword", "newPassword");

        assertThat(result).isTrue();
        assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
        verify(passwordEncoder).matches("oldPassword", "encodedOldPassword");
        verify(passwordEncoder).encode("newPassword");
        verify(userService).updateById(user);
    }

    @Test
    @DisplayName("changePassword 密码不匹配时抛出BusinessException(PASSWORD_CHANGE_ERROR)")
    void changePasswordThrowsWhenMismatch() {
        User user = createUser("u1", "testuser", "test@example.com", "13800138000");
        user.setPassword("encodedOldPassword");

        doReturn(user).when(userService).getCurrentUser();
        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        assertThatThrownBy(() -> userService.changePassword("wrongPassword", "newPassword"))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(UserErrorConstants.PASSWORD_CHANGE_ERROR);

        verify(passwordEncoder, never()).encode(anyString());
        verify(userService, never()).updateById(any(User.class));
    }

    // ================================================================
    // isUserBelongsSchool
    // ================================================================

    @Test
    @DisplayName("isUserBelongsSchool schoolId为null时抛出BusinessException(EMPTY_ARGS)")
    void isUserBelongsSchoolNullSchoolIdThrows() {
        assertThatThrownBy(() -> userService.isUserBelongsSchool("u1", null))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(UserErrorConstants.EMPTY_ARGS);
    }

    @Test
    @DisplayName("isUserBelongsSchool 用户存在且universityId匹配时返回true")
    void isUserBelongsSchoolReturnsTrueWhenMatch() {
        User user = createUser("u1", "testuser", "test@example.com", "13800138000");
        user.setUniversityId("school-1");

        doReturn(user).when(userService).getById("u1");

        Boolean result = userService.isUserBelongsSchool("u1", "school-1");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isUserBelongsSchool 用户不存在时返回false")
    void isUserBelongsSchoolReturnsFalseWhenUserNotFound() {
        doReturn(null).when(userService).getById("nonexistent");

        Boolean result = userService.isUserBelongsSchool("nonexistent", "school-1");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isUserBelongsSchool 用户存在但universityId为null时返回false")
    void isUserBelongsSchoolReturnsFalseWhenUniversityIdNull() {
        User user = createUser("u1", "testuser", "test@example.com", "13800138000");
        user.setUniversityId(null);

        doReturn(user).when(userService).getById("u1");

        Boolean result = userService.isUserBelongsSchool("u1", "school-1");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isUserBelongsSchool 用户存在但universityId不匹配时返回false")
    void isUserBelongsSchoolReturnsFalseWhenUniversityIdMismatch() {
        User user = createUser("u1", "testuser", "test@example.com", "13800138000");
        user.setUniversityId("other-school");

        doReturn(user).when(userService).getById("u1");

        Boolean result = userService.isUserBelongsSchool("u1", "school-1");

        assertThat(result).isFalse();
    }

    // ================================================================
    // getPage
    // ================================================================

    @Test
    @DisplayName("getPage 按条件分页查询并转换为 UserPublicVO")
    void getPageCallsConvertor() {
        UserDto dto = new UserDto();
        dto.setUsername("test");

        List<User> userList = List.of(createUser("u1", "testuser", "test@example.com", "13800138000"));
        Page<User> userPage = new Page<>(1, 10, 1);
        userPage.setRecords(userList);

        List<UserPublicVO> voList = List.of(new UserPublicVO());
        doReturn(userPage).when(userService).page(any(Page.class), any(LambdaQueryWrapper.class));
        when(convertor.toPublicVO(userList)).thenReturn(voList);

        Page<UserPublicVO> result = userService.getPage(dto, 1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getRecords()).isEqualTo(voList);
        verify(convertor).toPublicVO(userList);
    }

    @Test
    @DisplayName("getPage 空条件时返回全部用户")
    void getPageWithEmptyDto() {
        UserDto dto = new UserDto();

        Page<User> userPage = new Page<>(1, 10);
        doReturn(userPage).when(userService).page(any(Page.class), any(LambdaQueryWrapper.class));
        when(convertor.toPublicVO(anyList())).thenReturn(List.of());

        Page<UserPublicVO> result = userService.getPage(dto, 1, 10);

        assertThat(result).isNotNull();
        verify(convertor).toPublicVO(anyList());
    }

    // ================================================================
    // getAdminPage
    // ================================================================

    @Test
    @DisplayName("getAdminPage 按条件分页查询返回用户实体")
    void getAdminPageReturnsUserPage() {
        UserDto dto = new UserDto();
        dto.setRealName("张三");

        List<User> userList = List.of(createUser("u1", "testuser", "test@example.com", "13800138000"));
        Page<User> userPage = new Page<>(1, 10, 1);
        userPage.setRecords(userList);
        doReturn(userPage).when(userService).page(any(Page.class), any(LambdaQueryWrapper.class));

        Page<User> result = userService.getAdminPage(dto, 1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    @DisplayName("getAdminPage 空条件时返回所有用户")
    void getAdminPageWithEmptyDto() {
        UserDto dto = new UserDto();

        Page<User> userPage = new Page<>(1, 10);
        doReturn(userPage).when(userService).page(any(Page.class), any(LambdaQueryWrapper.class));

        Page<User> result = userService.getAdminPage(dto, 1, 10);

        assertThat(result).isNotNull();
    }
}
