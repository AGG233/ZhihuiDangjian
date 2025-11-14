package com.rauio.ZhihuiDangjiang.service.impl;

import com.rauio.ZhihuiDangjiang.dao.UserDao;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class UserTest {
    private final UserServiceImpl userServiceImpl;
    private final AuthServiceImpl authServiceImpl;
    private final UserDao         userDao;
    private final MockMvc mockMvc;

    @Autowired
    public UserTest(
            UserServiceImpl userServiceImpl,
            AuthServiceImpl authServiceImpl,
            UserDao         userDao,
            MockMvc mockMvc
                ) {
        this.userServiceImpl = userServiceImpl;
        this.authServiceImpl = authServiceImpl;
        this.userDao = userDao;
        this.mockMvc = mockMvc;
    }

    @Test
    public void testConnect() throws Exception {
        userDao.getUserByName("string");
    }
//    @Test
//    public void testRegister() {
//        RegisterRequest registerRequest = new RegisterRequest();
//        registerRequest.setUsername("test");
//        registerRequest.setPassword("<PASSWORD>");
//        registerRequest.setReal_name("test");
//        registerRequest.setParty_member_id("12345678901234567890");
//        registerRequest.setParty_status(UserStatus.fromDescription("积极分子"));
//        registerRequest.setBranch_name("test");
//        registerRequest.setEmail("test@example.com");
//        registerRequest.setPhone("12345678901");
//
//        User user = User.builder()
//                .username(registerRequest.getUsername())
//                .password(registerRequest.getPassword())
//                .realName(registerRequest.getReal_name())
//                .partyMemberId(registerRequest.getParty_member_id())
//                .partyStatus(registerRequest.getParty_status())
//                .branchName(registerRequest.getBranch_name())
//                .email(registerRequest.getEmail())
//                .phone(registerRequest.getPhone())
//                .build();
//
//        Boolean result = userDao.saveFile(user);
//        System.out.println("用户注册结果: " + result);
//        System.out.println("注册的用户: " + user);
//    }
}
