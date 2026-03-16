package com.rauio.smartdangjian.dao;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.smartdangjian.mapper.UserPermissionMapper;
import com.rauio.smartdangjian.pojo.UserPermission;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserPermissionDao {

    private final UserPermissionMapper userPermissionMapper;

    public int insert(UserPermission userPermission) {
        return userPermissionMapper.insert(userPermission);
    }

    public int update(UserPermission userPermission) {
        LambdaQueryWrapper<UserPermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPermission::getUserId, userPermission.getUserId());
        return userPermissionMapper.update(queryWrapper);
    }

    public UserPermission selectOne(UserPermission userPermission) {
        LambdaQueryWrapper<UserPermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPermission::getUserId, userPermission.getUserId());
        return userPermissionMapper.selectOne(queryWrapper);
    }

    public int delete(UserPermission userPermission) {
        LambdaQueryWrapper<UserPermission> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserPermission::getUserId, userPermission.getUserId());
        return userPermissionMapper.delete(queryWrapper);
    }
}
