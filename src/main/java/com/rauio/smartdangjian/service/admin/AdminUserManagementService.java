package com.rauio.smartdangjian.service.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.mapper.UserMapper;
import com.rauio.smartdangjian.pojo.User;
import com.rauio.smartdangjian.pojo.convertor.UserConvertor;
import com.rauio.smartdangjian.pojo.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 管理员用户统一应用服务，负责封装通用的用户管理流程。
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AdminUserManagementService extends ServiceImpl<UserMapper, User> {

    private final UserConvertor userConvertor;
    private final PasswordEncoder passwordEncoder;

    /**
     * 批量新增用户。
     *
     * @param dtoList 用户请求体列表
     * @return 是否新增成功
     */
    public Boolean addUsers(List<UserDto> dtoList) {
        List<User> users = userConvertor.toEntityList(dtoList);

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            UserDto dto = dtoList.get(i);
            applyDtoIdentity(user, dto);
            if (StringUtils.hasText(user.getPassword())) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        }
        return this.saveBatch(users);
    }

    /**
     * 批量更新用户。
     *
     * @param dtoList 用户请求体列表
     * @return 是否更新成功
     */
    public Boolean updateUsers(List<UserDto> dtoList) {
        List<User> users = userConvertor.toEntityList(dtoList);

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            UserDto dto = dtoList.get(i);
            applyDtoIdentity(user, dto);
            if (StringUtils.hasText(user.getPassword())) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
            }
        }
        return this.saveOrUpdateBatch(users);
    }

    /**
     * 批量删除用户。
     *
     * @param idList 用户 ID 列表
     * @return 是否删除成功
     */
    public Boolean deleteUsers(List<String> idList) {
        return this.removeByIds(idList);
    }

    /**
     * 获取单个用户。
     *
     * @param id 用户 ID
     * @return 用户信息
     */
    public User getUser(String id) {
        return this.getById(id);
    }

    /**
     * 分页查询用户。
     *
     * @param userDto 查询条件
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 用户分页结果
     */
    public Page<User> getUsers(UserDto userDto, int pageNum, int pageSize) {
        Page<User> pageInfo = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = buildUserQueryWrapper(userDto);
        return this.page(pageInfo, wrapper);
    }

    /**
     * 根据用户筛选条件构造查询包装器。
     *
     * @param userDto 查询条件
     * @return 查询包装器
     */
    private LambdaQueryWrapper<User> buildUserQueryWrapper(UserDto userDto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (userDto == null) {
            return wrapper;
        }

        wrapper.like(StringUtils.hasText(userDto.getUserId()), User::getId, userDto.getUserId())
                .like(StringUtils.hasText(userDto.getUsername()), User::getUsername, userDto.getUsername())
                .like(StringUtils.hasText(userDto.getPartyMemberId()), User::getPartyMemberId, userDto.getPartyMemberId())
                .like(StringUtils.hasText(userDto.getRealName()), User::getRealName, userDto.getRealName())
                .like(StringUtils.hasText(userDto.getPhone()), User::getPhone, userDto.getPhone())
                .like(StringUtils.hasText(userDto.getBranchName()), User::getBranchName, userDto.getBranchName())
                .like(StringUtils.hasText(userDto.getEmail()), User::getEmail, userDto.getEmail())
                .eq(StringUtils.hasText(userDto.getUniversityId()), User::getUniversityId, userDto.getUniversityId())
                .eq(userDto.getUserType() != null, User::getUserType, userDto.getUserType());

        return wrapper;
    }

    /**
     * 将 DTO 中的用户 ID 回填到实体主键字段，保证更新操作能够命中已有记录。
     *
     * @param user 用户实体
     * @param dto 用户请求体
     */
    private void applyDtoIdentity(User user, UserDto dto) {
        if (StringUtils.hasText(dto.getUserId())) {
            user.setId(dto.getUserId());
        }
    }
}
