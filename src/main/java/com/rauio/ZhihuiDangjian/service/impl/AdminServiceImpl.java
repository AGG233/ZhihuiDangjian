package com.rauio.ZhihuiDangjian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
@RequiredArgsConstructor
public class AdminServiceImpl extends ServiceImpl<UserMapper,User> implements AdminService{

    private final UserConvertor userConvertor;
    private final PasswordEncoder passwordEncoder;
    private final SqlSessionFactory sqlSessionFactory;
    private final UserMapper userMapper;
    private final UserService userService;

    // 系统管理员功能实现
    @Override
    public Boolean addUser(List<UserDto> dto) {
        LocalDateTime now = LocalDateTime.now();

        List<User> list = userConvertor.toEntityList(dto);
        for (User user : list) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setCreatedAt(now);
            user.setUpdatedAt(now);
        }
        return this.saveBatch(list);
    }

    @Override
    public Boolean deleteUser(List<String> idList) {
        return this.removeByIds(idList);
    }

    @Override
    public Boolean updateUser(List<UserDto> dto) {
        LocalDateTime now = LocalDateTime.now();

        List<User> list = userConvertor.toEntityList(dto);
        for (User user : list) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setUpdatedAt(now);
        }
        return this.saveOrUpdateBatch(list);
    }

    @Override
    public Page<User> getUser(UserDto userDto, int pageNum, int pageSize) {
        Page<User> pageInfo = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        if (userDto != null) {
            wrapper.like(User::getUsername, userDto.getUsername())
                    .like(User::getPartyMemberId, userDto.getPartyMemberId())
                    .like(User::getRealName, userDto.getRealName())
                    .like(User::getPhone, userDto.getPhone())
                    .like(User::getBranchName, userDto.getBranchName())
                    .like(User::getEmail, userDto.getEmail())
                    .like(User::getUserType, userDto.getUserType());
        }


        userMapper.selectPage(pageInfo, wrapper);
        return pageInfo;
    }

    // 学校管理员功能实现
    @Override
    public Boolean addSchoolUser(List<UserDto> userDtoList) {
        Long universityId = userService.getUserFromAuthentication().getUniversityId();

        for (UserDto user : userDtoList) {
            user.setUniversityId(universityId);
        }
        return this.addUser(userDtoList);
    }

    @Override
    public Boolean updateSchoolUser(List<UserDto> userDto) {
        Long universityId = userService.getUserFromAuthentication().getUniversityId();

        for (UserDto user : userDto) {
            if (!user.getUniversityId().equals(universityId)) throw new RuntimeException("用户：" +user.getUserId() +"与学校ID不匹配");
            if (user.getUserType() != UserType.STUDENT) throw new RuntimeException("用户：" +user.getUserId() +"不是学生");
        }

        return this.addUser(userDto);
    }

    @Override
    public Boolean deleteSchoolUser(List<String> userIdList) {
        Long universityId = userService.getUserFromAuthentication().getUniversityId();

        for (String id : userIdList) {
            if (!id.equals(String.valueOf(universityId))) throw new RuntimeException("用户：" + id +"与学校ID不匹配");
        }
        return this.removeByIds(userIdList);
    }

    @Override
    public User getSchoolUser(String id) {
        return userMapper.selectById(id);
    }

    @Override
    public Page<User> getSchoolUser(UserDto userDto, int pageNum, int pageSize) {
        Long universityId = userService.getUserFromAuthentication().getUniversityId();
        Page<User> pageInfo = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(User::getUniversityId, universityId)
                .like(User::getUsername, userDto.getUsername())
                .like(User::getPartyMemberId, userDto.getPartyMemberId())
                .like(User::getRealName, userDto.getRealName())
                .like(User::getPhone, userDto.getPhone())
                .like(User::getBranchName, userDto.getBranchName())
                .like(User::getEmail, userDto.getEmail())
                .like(User::getUserType, userDto.getUserType());

        userMapper.selectPage(pageInfo, wrapper);
        return pageInfo;
    }
}