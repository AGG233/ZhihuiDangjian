package com.rauio.ZhihuiDangjian.service.impl;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.ZhihuiDangjian.mapper.UserMapper;
import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.pojo.convertor.UserConvertor;
import com.rauio.ZhihuiDangjian.pojo.dto.UserDto;
import com.rauio.ZhihuiDangjian.service.AdminService;
import com.rauio.ZhihuiDangjian.service.UserService;
import com.rauio.ZhihuiDangjian.utils.Spec.UserType;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {

    private final UserConvertor  userConvertor;
    private final PasswordEncoder passwordEncoder;
    private final SqlSessionFactory sqlSessionFactory;
    private final UserMapper userMapper;
    private final UserService userService;

    @Override
    public String addUser(List<UserDto> user) {
        List<User> list = new ArrayList<>();
        for (UserDto dto : user) {
            dto.setUser_type(UserType.STUDENT);
            dto.setPassword(passwordEncoder.encode(dto.getPassword()));
            list.add(userConvertor.toEntity(dto));
        }

        MybatisBatch<User> batch = new MybatisBatch<>(sqlSessionFactory,list);
        MybatisBatch.Method<User> method = new  MybatisBatch.Method<>(User.class);

        return batch.execute(method.insert()).toString();
    }

    @Override
    public String deleteUser(List<String> idList) {
        MybatisBatch<String> batch = new MybatisBatch<>(sqlSessionFactory,idList);
        MybatisBatch.Method<User> method = new  MybatisBatch.Method<>(User.class);
        return batch.execute(method.deleteById()).toString();
    }

    @Override
    public String updateUser(List<UserDto> user) {

        List<User> list = new ArrayList<>();
        for (UserDto dto : user) {
            dto.setUser_type(UserType.STUDENT);
            dto.setPassword(passwordEncoder.encode(dto.getPassword()));
            list.add(userConvertor.toEntity(dto));
        }
        MybatisBatch<User> batch = new MybatisBatch<>(sqlSessionFactory,list);
        MybatisBatch.Method<User> method = new  MybatisBatch.Method<>(User.class);

        return batch.execute(method.updateById()).toString();
    }

    @Override
    public String addSchoolAdmin(List<UserDto> user) {
        List<User> list = new ArrayList<>();
        for (UserDto dto : user) {
            dto.setUser_type(UserType.TEACHER);
            dto.setPassword(passwordEncoder.encode(dto.getPassword()));
            list.add(userConvertor.toEntity(dto));
        }
        MybatisBatch<User> batch = new MybatisBatch<>(sqlSessionFactory,list);
        MybatisBatch.Method<User> method = new  MybatisBatch.Method<>(User.class);

        return batch.execute(method.insert()).toString();
    }

    @Override
    public String updateSchoolAdmin(List<UserDto> user) {
        List<User> list = new ArrayList<>();
        for (UserDto dto : user) {
            dto.setPassword(passwordEncoder.encode(dto.getPassword()));
            list.add(userConvertor.toEntity(dto));
        }
        MybatisBatch<User> batch = new MybatisBatch<>(sqlSessionFactory,list);
        MybatisBatch.Method<User> method = new  MybatisBatch.Method<>(User.class);

        return batch.execute(method.updateById()).toString();
    }

    @Override
    public String deleteSchoolAdmin(List<String> idList) {

        MybatisBatch<String> batch = new MybatisBatch<>(sqlSessionFactory,idList);
        MybatisBatch.Method<User> method = new  MybatisBatch.Method<>(User.class);

        return batch.execute(method.deleteById()).toString();
    }

    @Override
    public List<User> getUser(UserDto userDto) {
        String universityId = userService.getUserFromAuthentication().getUniversityId();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(User::getUniversityId,universityId)
                .like(User::getUsername,userDto.getUsername())
                .like(User::getPartyMemberId,userDto.getParty_member_id())
                .like(User::getRealName, userDto.getRealName())
                .like(User::getPhone, userDto.getPhone())
                .like(User::getBranchName,userDto.getBranch_name())
                .like(User::getEmail,userDto.getEmail())
                .like(User::getUserType,userDto.getUser_type());

        return userMapper.selectList(wrapper);
    }
}
