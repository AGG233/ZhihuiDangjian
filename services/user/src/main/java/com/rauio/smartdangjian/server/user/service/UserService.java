package com.rauio.smartdangjian.server.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.pojo.convertor.UserConvertor;
import com.rauio.smartdangjian.server.user.pojo.dto.UserDto;
import com.rauio.smartdangjian.server.user.pojo.vo.UserVO;
import com.rauio.smartdangjian.aop.annotation.RequireUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final PasswordEncoder   passwordEncoder;
    private final UserConvertor     convertor;

    /**
     * 根据用户 ID 获取用户信息。
     *
     * @param id 用户 ID
     * @return 用户视图对象
     */
    public UserVO get(String id) {
        return convertor.toVO(this.getById(id));
    }

    /**
     * 根据通行凭证识别并查询用户。
     *
     * @param passport 用户名、邮箱或手机号
     * @return 用户实体
     */
    public User getByPassport(String passport){
        if (passport == null || passport.isEmpty()) {
            return null;
        }
        if (passport.contains("@")){
            return getByEmail(passport);
        }
        if (passport.contains("+")){
            return getByPhone(passport);
        }else{
            return getByUsername(passport);
        }
    }

    /**
     * 获取当前登录用户。
     *
     * @return 当前调用接口的用户
     */
    @RequireUser
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return (User) principal;
            }
        }
        return null;
    }

    /**
     * 获取当前登录用户 ID。
     *
     * @return 当前用户 ID
     */
    @RequireUser
    public String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return ((User) principal).getId();
            }
        }
        return null;
    }

    /**
     * 根据用户名查询用户。
     *
     * @param username 用户名
     * @return 用户实体
     */
    public User getByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    /**
     * 根据邮箱查询用户。
     *
     * @param email 邮箱
     * @return 用户实体
     */
    public User getByEmail(String email) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
    }

    /**
     * 根据手机号查询用户。
     *
     * @param phone 手机号
     * @return 用户实体
     */
    public User getByPhone(String phone) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }

    /**
     * 根据党员编号查询用户。
     *
     * @param partyMemberId 党员编号
     * @return 用户实体
     */
    public User getByPartyMemberId(String partyMemberId) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getPartyMemberId, partyMemberId));
    }

    /**
     * 更新用户信息。
     *
     * @param id 用户 ID
     * @param user 用户实体
     * @return 是否更新成功
     */
    public Boolean update(String id,User user) {
        user.setId(id);
        return this.updateById(user);
    }

    /**
     * 删除用户。
     *
     * @param id 用户 ID
     * @return 是否删除成功
     */
    public Boolean delete(String id) {
        return this.removeById(id);
    }

    /**
     * 注册新用户。
     *
     * @param user 用户实体
     * @return 是否注册成功
     */
    public Boolean register(User user) {
        checkEmailRegistered(user.getEmail());
        checkPhoneRegistered(user.getPhone());
        checkUsernameOccupied(user.getUsername());
        checkPartyMemberId(String.valueOf(user.getPartyMemberId()));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return this.save(user);
    }

    /**
     * 修改当前用户密码。
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @return 是否修改成功
     */
    public Boolean changePassword(String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.isEmpty()){
            throw new BusinessException(ErrorConstants.ARGS_ERROR,"有空参数");
        }
        User user = getCurrentUser();
        if (user.getPassword().equals(oldPassword)){
            user.setPassword(passwordEncoder.encode(newPassword));
            return this.updateById(user);
        }
        throw new RuntimeException("修改密码时出现错误");
    }

    /**
     * @param id 用户ID
     * @param schoolId 学校id
     * @return 是否属于该学校
     */
    public Boolean isUserBelongsSchool(String id, String schoolId) {
        User user = getCurrentUser();
        
        if (schoolId == null || user == null) {
            throw new BusinessException(ErrorConstants.ARGS_ERROR,"有空参数");
        }
        return user.getUniversityId() != null && user.getUniversityId().equals(schoolId);
    }

    /**
     * 按条件分页查询用户。
     *
     * @param dto 查询条件
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 用户分页结果
     */
    public Page<User> getPage(UserDto dto, int pageNum, int pageSize) {

        Page<User> pageInfo = new Page<>(pageNum,pageSize);

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotColumnName(dto.getUserId()),User::getId,dto.getUserId())
                .like(StringUtils.isNotColumnName(dto.getUsername()),User::getUsername,dto.getUsername())
                .like(StringUtils.isNotColumnName(dto.getRealName()),User::getRealName,dto.getRealName())
                .like(StringUtils.isNotColumnName(dto.getPartyMemberId()),User::getPartyMemberId,dto.getPartyMemberId())
                .like(StringUtils.isNotColumnName(dto.getEmail()),User::getEmail,dto.getEmail())
                .like(StringUtils.isNotColumnName(dto.getPhone()),User::getPhone,dto.getPhone())
                .eq(User::getUserType,dto.getUserType())
                .eq(User::getPartyStatus,dto.getPartyStatus())
                .like(StringUtils.isNotColumnName(dto.getBranchName()),User::getBranchName,dto.getBranchName());

        return this.page(pageInfo,wrapper);
    }

    /**
     * 校验邮箱是否已注册。
     *
     * @param email 邮箱
     */
    private void checkEmailRegistered(String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>().eq(User::getEmail, email);
        if (this.exists(queryWrapper)) {
            throw new BusinessException(ErrorConstants.EMAIL_EXISTS,"该邮箱已被注册");
        }
    }

    /**
     * 校验手机号是否已注册。
     *
     * @param phone 手机号
     */
    private void checkPhoneRegistered(String phone) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>().eq(User::getPhone, phone);
        if (this.exists(queryWrapper)) {
            throw new BusinessException(ErrorConstants.PHONE_EXISTS, "该手机号已被注册");
        }
    }

    /**
     * 校验用户名是否已被占用。
     *
     * @param username 用户名
     */
    private void checkUsernameOccupied(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>().eq(User::getUsername, username);
        if (this.exists(queryWrapper)) {
            throw new BusinessException(ErrorConstants.USERNAME_EXISTS, "该昵称已被占用");
        }
    }

    /**
     * 校验党员编号是否已存在。
     *
     * @param partyMemberId 党员编号
     */
    private void checkPartyMemberId(String partyMemberId) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>().eq(User::getPartyMemberId, partyMemberId);
        if (this.exists(queryWrapper)) {
            throw new BusinessException(ErrorConstants.PARTY_MEMBER_ID_EXISTS, "党员编号已存在");
        }
    }
}
