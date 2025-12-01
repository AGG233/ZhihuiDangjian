package com.rauio.ZhihuiDangjian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.ZhihuiDangjian.dao.UserDao;
import com.rauio.ZhihuiDangjian.exception.BusinessException;
import com.rauio.ZhihuiDangjian.mapper.UserMapper;
import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.pojo.convertor.UserConvertor;
import com.rauio.ZhihuiDangjian.pojo.vo.UserVO;
import com.rauio.ZhihuiDangjian.utils.Spec.UserStatus;
import com.rauio.ZhihuiDangjian.utils.Spec.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户服务测试类")
public class UserTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserDao userDao;

    @Mock
    private UserConvertor convertor;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private String testUserId;

    @BeforeEach
    public void setUp() {
        // 准备测试数据
        testUserId = "test_user_" + System.currentTimeMillis();
        testUser = User.builder()
                .id(testUserId)
                .universityId("1001")
                .username("testuser_" + System.currentTimeMillis())
                .password("TestPassword123!")
                .realName("测试用户")
                .idCard("110101199001011234")
                .partyMemberId("PARTY" + System.currentTimeMillis())
                .joinPartyDate(new Date())
                .partyStatus(UserStatus.partyActivist)
                .branchName("测试支部")
                .userType(UserType.STUDENT)
                .status("active")
                .email("test_" + System.currentTimeMillis() + "@example.com")
                .phone("+86138" + (10000000 + (int)(Math.random() * 90000000)))
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
    }

    @Test
    @DisplayName("测试用户注册 - 成功")
    public void testRegister_Success() {
        // Given
        String rawPassword = testUser.getPassword();
        String encodedPassword = "$2a$10$encodedPassword";
        
        // Mock 所有的检查都通过
        when(userMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userDao.insert(testUser)).thenReturn(true);

        // When
        Boolean result = userService.register(testUser);

        // Then
        assertTrue(result, "用户注册应该成功");
        verify(passwordEncoder).encode(rawPassword);
        verify(userDao).insert(testUser);
    }

    @Test
    @DisplayName("测试用户注册 - 邮箱已存在")
    public void testRegister_EmailExists() {
        // Given - 模拟邮箱已存在（第一次检查邮箱时返回true）
        when(userMapper.exists(any(LambdaQueryWrapper.class))).thenReturn(true);

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(testUser);
        });
        assertEquals(BusinessException.EMAIL_EXISTS, exception.getCode());
    }

    @Test
    @DisplayName("测试用户注册 - 手机号已存在")
    public void testRegister_PhoneExists() {
        // Given - 模拟邮箱检查通过，手机号已存在
        when(userMapper.exists(any(LambdaQueryWrapper.class)))
            .thenReturn(false)  // 第一次调用：邮箱检查通过
            .thenReturn(true);  // 第二次调用：手机号已存在

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(testUser);
        });
        assertEquals(BusinessException.PHONE_EXISTS, exception.getCode());
    }

    @Test
    @DisplayName("测试用户注册 - 用户名已存在")
    public void testRegister_UsernameExists() {
        // Given - 模拟邮箱和手机号检查通过，用户名已存在
        when(userMapper.exists(any(LambdaQueryWrapper.class)))
            .thenReturn(false)  // 第一次调用：邮箱检查通过
            .thenReturn(false)  // 第二次调用：手机号检查通过
            .thenReturn(true);  // 第三次调用：用户名已存在

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(testUser);
        });
        assertEquals(BusinessException.USERNAME_EXISTS, exception.getCode());
    }

    @Test
    @DisplayName("测试用户注册 - 党员编号已存在")
    public void testRegister_PartyMemberIdExists() {
        // Given - 模拟前三项检查通过，党员编号已存在
        when(userMapper.exists(any(LambdaQueryWrapper.class)))
            .thenReturn(false)  // 第一次调用：邮箱检查通过
            .thenReturn(false)  // 第二次调用：手机号检查通过
            .thenReturn(false)  // 第三次调用：用户名检查通过
            .thenReturn(true);  // 第四次调用：党员编号已存在

        // When & Then
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            userService.register(testUser);
        });
        assertEquals(BusinessException.PARTY_MEMBER_ID_EXISTS, exception.getCode());
    }

    @Test
    @DisplayName("测试根据ID获取用户")
    public void testGetUserByID() {
        // Given
        UserVO userVO = new UserVO();
        userVO.setId(testUser.getId());
        userVO.setUsername(testUser.getUsername());
        userVO.setRealName(testUser.getRealName());
        
        when(userDao.get(testUser.getId())).thenReturn(testUser);
        when(convertor.toVO(testUser)).thenReturn(userVO);

        // When
        UserVO result = userService.getUserByID(testUser.getId());

        // Then
        assertNotNull(result, "用户信息不应为空");
        assertEquals(testUser.getUsername(), result.getUsername());
        assertEquals(testUser.getRealName(), result.getRealName());
        verify(userDao).get(testUser.getId());
    }

    @Test
    @DisplayName("测试根据用户名获取用户")
    public void testGetUserByName() {
        // Given
        when(userDao.getUserByName(testUser.getUsername())).thenReturn(testUser);

        // When
        User foundUser = userService.getUserByName(testUser.getUsername());

        // Then
        assertNotNull(foundUser, "用户不应为空");
        assertEquals(testUser.getUsername(), foundUser.getUsername());
        assertEquals(testUser.getEmail(), foundUser.getEmail());
        verify(userDao).getUserByName(testUser.getUsername());
    }

    @Test
    @DisplayName("测试根据邮箱获取用户")
    public void testGetUserByEmail() {
        // Given
        when(userDao.getUserByEmail(testUser.getEmail())).thenReturn(testUser);

        // When
        User foundUser = userService.getUserByEmail(testUser.getEmail());

        // Then
        assertNotNull(foundUser, "用户不应为空");
        assertEquals(testUser.getEmail(), foundUser.getEmail());
        assertEquals(testUser.getUsername(), foundUser.getUsername());
        verify(userDao).getUserByEmail(testUser.getEmail());
    }

    @Test
    @DisplayName("测试根据手机号获取用户")
    public void testGetUserByPhone() {
        // Given
        when(userDao.getUserByPhone(testUser.getPhone())).thenReturn(testUser);

        // When
        User foundUser = userService.getUserByPhone(testUser.getPhone());

        // Then
        assertNotNull(foundUser, "用户不应为空");
        assertEquals(testUser.getPhone(), foundUser.getPhone());
        assertEquals(testUser.getUsername(), foundUser.getUsername());
        verify(userDao).getUserByPhone(testUser.getPhone());
    }

    @Test
    @DisplayName("测试根据党员编号获取用户")
    public void testGetUserByPartyMemberId() {
        // Given
        when(userDao.getUserByPartyMemberId(testUser.getPartyMemberId())).thenReturn(testUser);

        // When
        User foundUser = userService.getUserByPartyMemberId(testUser.getPartyMemberId());

        // Then
        assertNotNull(foundUser, "用户不应为空");
        assertEquals(testUser.getPartyMemberId(), foundUser.getPartyMemberId());
        assertEquals(testUser.getUsername(), foundUser.getUsername());
        verify(userDao).getUserByPartyMemberId(testUser.getPartyMemberId());
    }

    @Test
    @DisplayName("测试通用获取用户 - 通过邮箱")
    public void testGetUserByAll_Email() {
        // Given
        when(userDao.getUserByEmail(testUser.getEmail())).thenReturn(testUser);

        // When
        User foundUser = userService.getUserByAll(testUser.getEmail());

        // Then
        assertNotNull(foundUser, "用户不应为空");
        assertEquals(testUser.getEmail(), foundUser.getEmail());
    }

    @Test
    @DisplayName("测试通用获取用户 - 通过手机号")
    public void testGetUserByAll_Phone() {
        // Given
        when(userDao.getUserByPhone(testUser.getPhone())).thenReturn(testUser);

        // When
        User foundUser = userService.getUserByAll(testUser.getPhone());

        // Then
        assertNotNull(foundUser, "用户不应为空");
        assertEquals(testUser.getPhone(), foundUser.getPhone());
    }

    @Test
    @DisplayName("测试通用获取用户 - 通过用户名")
    public void testGetUserByAll_Username() {
        // Given
        when(userDao.getUserByName(testUser.getUsername())).thenReturn(testUser);

        // When
        User foundUser = userService.getUserByAll(testUser.getUsername());

        // Then
        assertNotNull(foundUser, "用户不应为空");
        assertEquals(testUser.getUsername(), foundUser.getUsername());
    }

    @Test
    @DisplayName("测试通用获取用户 - 空参数")
    public void testGetUserByAll_NullOrEmpty() {
        // When & Then
        assertNull(userService.getUserByAll(null));
        assertNull(userService.getUserByAll(""));
    }

    @Test
    @DisplayName("测试更新用户信息")
    public void testUpdateUser() {
        // Given
        String newRealName = "更新后的姓名";
        testUser.setRealName(newRealName);
        when(userDao.update(testUser)).thenReturn(true);

        // When
        Boolean result = userService.update(testUser.getId(), testUser);

        // Then
        assertTrue(result, "更新应该成功");
        verify(userDao).update(testUser);
    }

    @Test
    @DisplayName("测试修改密码")
    public void testChangePassword() {
        // Given
        String newPassword = "$2a$10$newEncodedPassword";
        when(userDao.changePassword(testUser.getId(), newPassword)).thenReturn(true);

        // When
        Boolean result = userService.changePassword(testUser.getId(), newPassword);

        // Then
        assertTrue(result, "修改密码应该成功");
        verify(userDao).changePassword(testUser.getId(), newPassword);
    }

    @Test
    @DisplayName("测试删除用户")
    public void testDeleteUser() {
        // Given
        when(userDao.delete(testUser.getId())).thenReturn(true);

        // When
        Boolean result = userService.delete(testUser.getId());

        // Then
        assertTrue(result, "删除应该成功");
        verify(userDao).delete(testUser.getId());
    }

    @Test
    @DisplayName("测试删除不存在的用户")
    public void testDeleteNonExistentUser() {
        // Given
        String nonExistentId = "non_existent_id";
        when(userDao.delete(nonExistentId)).thenReturn(false);

        // When
        Boolean result = userService.delete(nonExistentId);

        // Then
        assertFalse(result, "删除不存在的用户应该返回false");
        verify(userDao).delete(nonExistentId);
    }

    @Test
    @DisplayName("测试获取不存在的用户 - 根据用户名")
    public void testGetNonExistentUserByName() {
        // Given
        when(userDao.getUserByName("non_existent_username")).thenReturn(null);

        // When
        User user = userService.getUserByName("non_existent_username");

        // Then
        assertNull(user, "不存在的用户应该返回null");
    }

    @Test
    @DisplayName("测试获取不存在的用户 - 根据邮箱")
    public void testGetNonExistentUserByEmail() {
        // Given
        when(userDao.getUserByEmail("nonexistent@example.com")).thenReturn(null);

        // When
        User user = userService.getUserByEmail("nonexistent@example.com");

        // Then
        assertNull(user, "不存在的用户应该返回null");
    }

    @Test
    @DisplayName("测试用户状态枚举")
    public void testUserStatusEnum() {
        // Given
        User user = User.builder()
                .id("status_test_user")
                .username("statustest")
                .password("Password123!")
                .email("status@example.com")
                .phone("+8613900000099")
                .partyMemberId("PARTY123456789")
                .realName("状态测试")
                .partyStatus(UserStatus.formalMEMBER)
                .branchName("测试支部")
                .userType(UserType.STUDENT)
                .build();

        // Then
        assertEquals(UserStatus.formalMEMBER, user.getPartyStatus());
        assertEquals("正式党员", user.getPartyStatus().getDescription());
    }

    @Test
    @DisplayName("测试用户类型枚举")
    public void testUserTypeEnum() {
        // Given - 确保 testUser 的 userType 已设置
        assertNotNull(testUser.getUserType(), "userType 不应为null");
        assertEquals(UserType.STUDENT, testUser.getUserType());
        
        // Then - Test authorities
        Collection<? extends GrantedAuthority> authorities = testUser.getAuthorities();
        assertNotNull(authorities, "权限不应为null");
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_STUDENT")));
    }
}
