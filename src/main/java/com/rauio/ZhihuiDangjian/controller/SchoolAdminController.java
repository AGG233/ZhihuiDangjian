package com.rauio.ZhihuiDangjian.controller;


import com.rauio.ZhihuiDangjian.aop.annotation.PermissionAccess;
import com.rauio.ZhihuiDangjian.mapper.UserMapper;
import com.rauio.ZhihuiDangjian.service.UserService;
import com.rauio.ZhihuiDangjian.utils.Spec.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@RestController("/school/admin")
@RequiredArgsConstructor
@Slf4j
@PermissionAccess(UserType.TEACHER)
public class SchoolAdminController {

    private final UserMapper userMapper;
    private final UserService userService;

}
