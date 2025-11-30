package com.rauio.ZhihuiDangjian.service.impl;

import com.baomidou.mybatisplus.core.batch.MybatisBatch;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.ZhihuiDangjian.mapper.UserMapper;
import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.pojo.convertor.UserConvertor;
import com.rauio.ZhihuiDangjian.pojo.dto.UserDto;
import com.rauio.ZhihuiDangjian.pojo.vo.UserVO;
import com.rauio.ZhihuiDangjian.service.SchoolAdminService;
import com.rauio.ZhihuiDangjian.service.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional(rollbackFor = Exception.class,propagation = Propagation.REQUIRES_NEW)
@RequiredArgsConstructor
public class SchoolAdminServiceImpl implements SchoolAdminService {

    private final UserMapper userMapper;
    private final UserConvertor userConvertor;
    private final UserService userService;
    private final SqlSessionFactory sqlSessionFactory;
    private final PasswordEncoder passwordEncoder;


    private final String universityId = userService.getUserFromAuthentication().getUniversityId();
    @Override
    public int addUser(List<UserDto> userDtoList) {

        List<User> userList = userConvertor.toEntityList(userDtoList);
        for (User user : userList) {
            user.setUniversityId(universityId);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        MybatisBatch<User> batch = new MybatisBatch<>(sqlSessionFactory,userList);
        MybatisBatch.Method<User> method = new  MybatisBatch.Method<>(User.class);
        return batch.execute(method.insert()).size();
    }

    @Override
    public int updateUser(List<UserDto> userDto) {

        List<User> userList = userConvertor.toEntityList(userDto);
        for (User user : userList) {

            if (!user.getUniversityId().equals(universityId)) return -1;

            user.setUniversityId(universityId);
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        MybatisBatch<User> batch = new MybatisBatch<>(sqlSessionFactory,userList);
        MybatisBatch.Method<User> method = new  MybatisBatch.Method<>(User.class);
        return batch.execute(method.updateById()).size();
    }

    @Override
    public int deleteUser(List<String> userIdList) {
        MybatisBatch<String> batch = new MybatisBatch<>(sqlSessionFactory,userIdList);
        MybatisBatch.Method<User> method = new  MybatisBatch.Method<>(User.class);
        return batch.execute(method.deleteById()).size();
    }

    @Override
    public UserVO getUser(UserDto userDto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(User::getUniversityId,universityId)
                .like(User::getUsername,userDto.getUsername())
                .like(User::getPartyMemberId,userDto.getParty_member_id())
                .like(User::getRealName, userDto.getRealName())
                .like(User::getPhone, userDto.getPhone())
                .like(User::getBranchName,userDto.getBranch_name())
                .like(User::getEmail,userDto.getEmail())
                .like(User::getUserType,userDto.getUser_type());

        return userConvertor.toVO(userMapper.selectOne(wrapper));
    }
}
