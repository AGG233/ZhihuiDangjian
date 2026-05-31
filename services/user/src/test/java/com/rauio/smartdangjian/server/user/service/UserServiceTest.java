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
import static org.mockito.Mockito.reset;

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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.user.constants.UserErrorConstants;
import com.rauio.smartdangjian.server.user.pojo.convertor.UserConvertor;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.pojo.request.UserRequest;
import com.rauio.smartdangjian.server.user.pojo.response.UserPublicResponse;
import com.rauio.smartdangjian.server.user.pojo.response.UserResponse;
import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;
import com.rauio.smartdangjian.utils.spec.UserType;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.crypto.digest.BCrypt;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserConvertor convertor;

    @Spy
    @InjectMocks
    private UserService userService;

    @BeforeEach
    void resetSpy() {
        reset(userService);
    }

    // ---------- helpers ----------

    private User createUser(Long id, String username, String email, String phone) {
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
    @DisplayName("getByPassport passport为null时抛出BusinessException(EMPTY_ARGS)")
    void getByPassportNullThrows() {
        assertThatThrownBy(() -> userService.getByPassport(null))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(UserErrorConstants.EMPTY_ARGS);
    }

    @Test
    @DisplayName("getByPassport passport为空字符串时抛出BusinessException(EMPTY_ARGS)")
    void getByPassportEmptyThrows() {
        assertThatThrownBy(() -> userService.getByPassport(""))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(UserErrorConstants.EMPTY_ARGS);
    }

    @Test
    @DisplayName("getByPassport passport包含@时调用getByEmail并返回结果")
    void getByPassportWithAtDelegatesToEmail() {
        User expectedUser = createUser(1L, "testuser", "test@example.com", "13800138000");
        doReturn(expectedUser).when(userService).getByEmail("test@example.com");

        User result = userService.getByPassport("test@example.com");

        assertThat(result).isEqualTo(expectedUser);
    }

    @Test
    @DisplayName("getByPassport passport包含+时调用getByPhone并返回结果")
    void getByPassportWithPlusDelegatesToPhone() {
        User expectedUser = createUser(1L, "testuser", "test@example.com", "+8613800138000");
        doReturn(expectedUser).when(userService).getByPhone("+8613800138000");

        User result = userService.getByPassport("+8613800138000");

        assertThat(result).isEqualTo(expectedUser);
    }

    @Test
    @DisplayName("getByPassport passport为普通字符串时调用getByUsername并返回结果")
    void getByPassportPlainDelegatesToUsername() {
        User expectedUser = createUser(1L, "testuser", "test@example.com", "13800138000");
        doReturn(expectedUser).when(userService).getByUsername("testuser");

        User result = userService.getByPassport("testuser");

        assertThat(result).isEqualTo(expectedUser);
    }

    // ================================================================
    // get
    // ================================================================

    @Test
    @DisplayName("get 根据ID调用getById并转换为UserResponse返回")
    void getByIdConvertsToVO() {
        User user = createUser(1L, "testuser", "test@example.com", "13800138000");
        UserResponse expectedVO = new UserResponse();
        expectedVO.setId(1L);
        expectedVO.setUsername("testuser");

        doReturn(user).when(userService).getById(1L);
        when(convertor.toResponse(user)).thenReturn(expectedVO);

        UserResponse result = userService.get(1L);

        assertThat(result).isEqualTo(expectedVO);
        verify(convertor).toResponse(user);
    }

    // ================================================================
    // getCurrentUser
    // ================================================================

    @Test
    @DisplayName("getCurrentUser 已登录且session中有User时返回该User")
    void getCurrentUserAuthenticatedReturnsUser() {
        User user = createUser(1L, "testuser", "test@example.com", "13800138000");

        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            SaSession session = mock(SaSession.class);
            when(session.get("user")).thenReturn(user);
            stpUtilMock.when(StpUtil::isLogin).thenReturn(true);
            stpUtilMock.when(StpUtil::getSession).thenReturn(session);

            User result = userService.getCurrentUser();

            assertThat(result).isEqualTo(user);
        }
    }

    @Test
    @DisplayName("getCurrentUser session中的user不是User实例时返回null")
    void getCurrentUserPrincipalNotUserReturnsNull() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            SaSession session = mock(SaSession.class);
            when(session.get("user")).thenReturn("not-a-user-instance");
            stpUtilMock.when(StpUtil::isLogin).thenReturn(true);
            stpUtilMock.when(StpUtil::getSession).thenReturn(session);

            User result = userService.getCurrentUser();

            assertThat(result).isNull();
        }
    }

    @Test
    @DisplayName("getCurrentUser 未登录时返回null")
    void getCurrentUserNullAuthenticationReturnsNull() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::isLogin).thenReturn(false);

            User result = userService.getCurrentUser();

            assertThat(result).isNull();
        }
    }

    // ================================================================
    // getCurrentUserId
    // ================================================================

    @Test
    @DisplayName("getCurrentUserId 已登录时返回用户ID")
    void getCurrentUserIdAuthenticatedReturnsId() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::isLogin).thenReturn(true);
            stpUtilMock.when(StpUtil::getLoginIdAsString).thenReturn("user-id-123");

            String result = userService.getCurrentUserId();

            assertThat(result).isEqualTo("user-id-123");
        }
    }

    @Test
    @DisplayName("getCurrentUserId 未登录时返回null")
    void getCurrentUserIdNotAuthenticatedReturnsDefaultId() {
        try (MockedStatic<StpUtil> stpUtilMock = mockStatic(StpUtil.class)) {
            stpUtilMock.when(StpUtil::isLogin).thenReturn(false);

            String result = userService.getCurrentUserId();

            assertThat(result).isNull();
        }
    }

    // ================================================================
    // getByUsername
    // ================================================================

    @Test
    @DisplayName("getByUsername 调用getOne查询并返回用户")
    void getByUsernameReturnsUser() {
        User expectedUser = createUser(1L, "testuser", "test@example.com", "13800138000");
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
        User expectedUser = createUser(1L, "testuser", "test@example.com", "13800138000");
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
        User expectedUser = createUser(1L, "testuser", "test@example.com", "13800138000");
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
        User expectedUser = createUser(1L, "testuser", "test@example.com", "13800138000");
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
        doReturn(user).when(userService).getById(1L);

        userService.update(1L, user);

        assertThat(user.getId()).isEqualTo(1L);
        verify(userService).updateById(user);
    }

    @Test
    @DisplayName("update 有密码时加密后更新")
    void updateWithPasswordEncodesAndUpdates() {
        User user = createUser(null, "testuser", "test@example.com", "13800138000");
        user.setPassword("plainPassword");

        try (MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock.when(() -> BCrypt.hashpw("plainPassword")).thenReturn("encodedNewPassword");
            doReturn(true).when(userService).updateById(any(User.class));
            doReturn(user).when(userService).getById(1L);

            userService.update(1L, user);

            assertThat(user.getId()).isEqualTo(1L);
            assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
            bcryptMock.verify(() -> BCrypt.hashpw("plainPassword"));
        }
        verify(userService).updateById(user);
    }

    @Test
    @DisplayName("update updateById 返回 false 时抛出 BusinessException")
    void updateFailsWhenUpdateByIdReturnsFalse() {
        User user = createUser(null, "testuser", "test@example.com", "13800138000");
        user.setPassword(null);

        doReturn(false).when(userService).updateById(any(User.class));

        assertThatThrownBy(() -> userService.update(1L, user))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(UserErrorConstants.USER_NOT_EXISTS);
    }

    @Test
    @DisplayName("update 密码为空字符串时不加密")
    void updateWithEmptyPasswordDoesNotEncode() {
        User user = createUser(null, "testuser", "test@example.com", "13800138000");
        user.setPassword("");

        doReturn(true).when(userService).updateById(any(User.class));
        doReturn(user).when(userService).getById(1L);

        try (MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            userService.update(1L, user);

            bcryptMock.verify(() -> BCrypt.hashpw(anyString()), never());
        }
    }

    // ================================================================
    // delete
    // ================================================================

    @Test
    @DisplayName("delete 调用removeById删除")
    void deleteCallsRemoveById() {
        doReturn(true).when(userService).removeById(1L);

        userService.delete(1L);

        verify(userService).removeById(1L);
    }

    @Test
    @DisplayName("delete 删除不存在的用户时抛出 BusinessException")
    void deleteNonExistentThrows() {
        doReturn(false).when(userService).removeById(9999L);

        assertThatThrownBy(() -> userService.delete(9999L))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(UserErrorConstants.USER_NOT_EXISTS);
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
        doReturn(true).when(userService).save(any(User.class));

        try (MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock.when(() -> BCrypt.hashpw("plainPassword")).thenReturn("encodedPassword");

            userService.register(user);

            assertThat(user.getPassword()).isEqualTo("encodedPassword");
            bcryptMock.verify(() -> BCrypt.hashpw("plainPassword"));
        }
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
    @DisplayName("register 用户为 null 时抛出BusinessException(EMPTY_ARGS)")
    void registerThrowsWhenUserIsNull() {
        assertThatThrownBy(() -> userService.register(null))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(UserErrorConstants.EMPTY_ARGS);
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

    @Test
    @DisplayName("register save 返回 false 时抛出 BusinessException")
    void registerFailsWhenSaveReturnsFalse() {
        User user = createUser(null, "newuser", "new@example.com", "13900139000");
        user.setPassword("plainPassword");

        doReturn(false).when(userService).exists(any(LambdaQueryWrapper.class));
        doReturn(false).when(userService).save(any(User.class));

        try (MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock.when(() -> BCrypt.hashpw("plainPassword")).thenReturn("encodedPassword");

            assertThatThrownBy(() -> userService.register(user))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(UserErrorConstants.USER_NOT_EXISTS);
        }
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
        User user = createUser(1L, "testuser", "test@example.com", "13800138000");
        user.setPassword("encodedOldPassword");

        doReturn(user).when(userService).getCurrentUser();
        doReturn(true).when(userService).updateById(any(User.class));

        try (MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock
                    .when(() -> BCrypt.checkpw("oldPassword", "encodedOldPassword"))
                    .thenReturn(true);
            bcryptMock.when(() -> BCrypt.hashpw("newPassword")).thenReturn("encodedNewPassword");

            userService.changePassword("oldPassword", "newPassword");

            assertThat(user.getPassword()).isEqualTo("encodedNewPassword");
            bcryptMock.verify(() -> BCrypt.checkpw("oldPassword", "encodedOldPassword"));
            bcryptMock.verify(() -> BCrypt.hashpw("newPassword"));
        }
        verify(userService).updateById(user);
    }

    @Test
    @DisplayName("changePassword 密码不匹配时抛出BusinessException(PASSWORD_CHANGE_ERROR)")
    void changePasswordThrowsWhenMismatch() {
        User user = createUser(1L, "testuser", "test@example.com", "13800138000");
        user.setPassword("encodedOldPassword");

        doReturn(user).when(userService).getCurrentUser();

        try (MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock
                    .when(() -> BCrypt.checkpw("wrongPassword", "encodedOldPassword"))
                    .thenReturn(false);

            assertThatThrownBy(() -> userService.changePassword("wrongPassword", "newPassword"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(UserErrorConstants.PASSWORD_CHANGE_ERROR);

            bcryptMock.verify(() -> BCrypt.hashpw(anyString()), never());
        }
        verify(userService, never()).updateById(any(User.class));
    }

    @Test
    @DisplayName("changePassword 密码匹配但 updateById 返回 false 时抛出 BusinessException")
    void changePasswordFailsWhenUpdateByIdReturnsFalse() {
        User user = createUser(1L, "testuser", "test@example.com", "13800138000");
        user.setPassword("encodedOldPassword");

        doReturn(user).when(userService).getCurrentUser();
        doReturn(false).when(userService).updateById(any(User.class));

        try (MockedStatic<BCrypt> bcryptMock = mockStatic(BCrypt.class)) {
            bcryptMock
                    .when(() -> BCrypt.checkpw("oldPassword", "encodedOldPassword"))
                    .thenReturn(true);
            bcryptMock.when(() -> BCrypt.hashpw("newPassword")).thenReturn("encodedNewPassword");

            assertThatThrownBy(() -> userService.changePassword("oldPassword", "newPassword"))
                    .isInstanceOf(BusinessException.class)
                    .extracting("code")
                    .isEqualTo(UserErrorConstants.PASSWORD_CHANGE_ERROR);
        }
    }

    // ================================================================
    // isUserBelongsSchool
    // ================================================================

    @Test
    @DisplayName("isUserBelongsSchool schoolId为null时抛出BusinessException(EMPTY_ARGS)")
    void isUserBelongsSchoolNullSchoolIdThrows() {
        assertThatThrownBy(() -> userService.isUserBelongsSchool(1L, null))
                .isInstanceOf(BusinessException.class)
                .extracting("code")
                .isEqualTo(UserErrorConstants.EMPTY_ARGS);
    }

    @Test
    @DisplayName("isUserBelongsSchool 用户存在且universityId匹配时返回true")
    void isUserBelongsSchoolReturnsTrueWhenMatch() {
        User user = createUser(1L, "testuser", "test@example.com", "13800138000");
        user.setUniversityId("1");

        doReturn(user).when(userService).getById(1L);

        Boolean result = userService.isUserBelongsSchool(1L, "1");

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isUserBelongsSchool 用户不存在时返回false")
    void isUserBelongsSchoolReturnsFalseWhenUserNotFound() {
        doReturn(null).when(userService).getById(9999L);

        Boolean result = userService.isUserBelongsSchool(9999L, "1");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isUserBelongsSchool 用户存在但universityId为null时返回false")
    void isUserBelongsSchoolReturnsFalseWhenUniversityIdNull() {
        User user = createUser(1L, "testuser", "test@example.com", "13800138000");
        user.setUniversityId(null);

        doReturn(user).when(userService).getById(1L);

        Boolean result = userService.isUserBelongsSchool(1L, "1");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isUserBelongsSchool 用户存在但universityId不匹配时返回false")
    void isUserBelongsSchoolReturnsFalseWhenUniversityIdMismatch() {
        User user = createUser(1L, "testuser", "test@example.com", "13800138000");
        user.setUniversityId("other-school");

        doReturn(user).when(userService).getById(1L);

        Boolean result = userService.isUserBelongsSchool(1L, "1");

        assertThat(result).isFalse();
    }

    // ================================================================
    // getPage
    // ================================================================

    @Test
    @DisplayName("getPage 按条件分页查询并转换为 UserPublicResponse")
    void getPageCallsConvertor() {
        UserRequest request = new UserRequest();
        request.setUsername("test");

        List<User> userList = List.of(createUser(1L, "testuser", "test@example.com", "13800138000"));
        Page<User> userPage = new Page<>(1, 10, 1);
        userPage.setRecords(userList);

        List<UserPublicResponse> responseList = List.of(new UserPublicResponse());
        doReturn(userPage).when(userService).page(any(Page.class), any(LambdaQueryWrapper.class));
        when(convertor.toPublicResponse(userList)).thenReturn(responseList);

        Page<UserPublicResponse> result = userService.getPage(request, 1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getRecords()).isEqualTo(responseList);
        verify(convertor).toPublicResponse(userList);
    }

    @Test
    @DisplayName("getPage 空条件时返回全部用户")
    void getPageWithEmptyDto() {
        UserRequest request = new UserRequest();

        Page<User> userPage = new Page<>(1, 10);
        doReturn(userPage).when(userService).page(any(Page.class), any(LambdaQueryWrapper.class));
        when(convertor.toPublicResponse(anyList())).thenReturn(List.of());

        Page<UserPublicResponse> result = userService.getPage(request, 1, 10);

        assertThat(result).isNotNull();
        verify(convertor).toPublicResponse(anyList());
    }

    // ================================================================
    // getAdminPage
    // ================================================================

    @Test
    @DisplayName("getAdminPage 按条件分页查询返回用户实体")
    void getAdminPageReturnsUserPage() {
        UserRequest request = new UserRequest();
        request.setRealName("张三");

        List<User> userList = List.of(createUser(1L, "testuser", "test@example.com", "13800138000"));
        Page<User> userPage = new Page<>(1, 10, 1);
        userPage.setRecords(userList);
        doReturn(userPage).when(userService).page(any(Page.class), any(LambdaQueryWrapper.class));

        Page<User> result = userService.getAdminPage(request, 1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(1);
    }

    @Test
    @DisplayName("getAdminPage 空条件时返回所有用户")
    void getAdminPageWithEmptyDto() {
        UserRequest request = new UserRequest();

        Page<User> userPage = new Page<>(1, 10);
        doReturn(userPage).when(userService).page(any(Page.class), any(LambdaQueryWrapper.class));

        Page<User> result = userService.getAdminPage(request, 1, 10);

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("getPage 条件中包含 userType 和 partyStatus 时正常查询")
    void getPageWithUserTypeAndPartyStatus() {
        UserRequest request = new UserRequest();
        request.setUserType(UserType.STUDENT);
        request.setPartyStatus(PartyStatus.FORMAL_MEMBER);

        List<User> userList = List.of(createUser(1L, "testuser", "test@example.com", "13800138000"));
        Page<User> userPage = new Page<>(1, 10, 1);
        userPage.setRecords(userList);

        List<UserPublicResponse> responseList = List.of(new UserPublicResponse());
        doReturn(userPage).when(userService).page(any(Page.class), any(LambdaQueryWrapper.class));
        when(convertor.toPublicResponse(userList)).thenReturn(responseList);

        Page<UserPublicResponse> result = userService.getPage(request, 1, 10);

        assertThat(result).isNotNull();
        assertThat(result.getRecords()).isEqualTo(responseList);
    }
}
