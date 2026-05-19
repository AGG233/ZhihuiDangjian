package com.rauio.smartdangjian.server.user.service;

import static com.rauio.smartdangjian.constants.RedisConstants.USER_VO_CACHE_PREFIX;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.aop.annotation.RequireUser;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.server.user.constants.UserErrorConstants;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.convertor.UserConvertor;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.pojo.request.UserRequest;
import com.rauio.smartdangjian.server.user.pojo.response.UserPublicResponse;
import com.rauio.smartdangjian.server.user.pojo.response.UserResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final PasswordEncoder passwordEncoder;
    private final UserConvertor convertor;

    @Value("${app.dev.default-user-id:}")
    private String defaultDevUserId;

    /**
     * 根据通行凭证识别并查询用户。
     *
     * @param passport 用户名、邮箱或手机号
     * @return 用户实体
     * @throws BusinessException 如果通行凭证为空
     */
    @Cacheable(value = USER_VO_CACHE_PREFIX, key = "#passport")
    public User getByPassport(String passport) {
        if (passport == null || passport.isEmpty()) {
            throw new BusinessException(UserErrorConstants.EMPTY_ARGS, "通行凭证不能为空");
        }
        if (passport.contains("@")) {
            return getByEmail(passport);
        }
        if (passport.contains("+")) {
            return getByPhone(passport);
        } else {
            return getByUsername(passport);
        }
    }

    /**
     * 根据用户 ID 获取用户信息。
     *
     * @param id 用户 ID
     * @return 用户视图对象
     */
    @Cacheable(value = USER_VO_CACHE_PREFIX, key = "#id")
    public UserResponse get(String id) {
        return convertor.toResponse(this.getById(id));
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
     * @return 当前用户 ID，未登录时返回开发环境默认值（如有配置）
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
        return defaultDevUserId;
    }

    /**
     * 根据用户名查询用户。
     *
     * @param username 用户名
     * @return 用户实体
     */
    @Cacheable(value = USER_VO_CACHE_PREFIX, key = "#username")
    public User getByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    /**
     * 根据邮箱查询用户。
     *
     * @param email 邮箱
     * @return 用户实体
     */
    @Cacheable(value = USER_VO_CACHE_PREFIX, key = "#email")
    public User getByEmail(String email) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
    }

    /**
     * 根据手机号查询用户。
     *
     * @param phone 手机号
     * @return 用户实体
     */
    @Cacheable(value = USER_VO_CACHE_PREFIX, key = "#phone")
    public User getByPhone(String phone) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }

    /**
     * 根据党员编号查询用户。
     *
     * @param partyMemberId 党员编号
     * @return 用户实体
     */
    @Cacheable(value = USER_VO_CACHE_PREFIX, key = "#partyMemberId")
    public User getByPartyMemberId(String partyMemberId) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getPartyMemberId, partyMemberId));
    }

    /**
     * 更新用户信息。
     *
     * @param id 用户 ID
     * @param user 用户实体
     * @throws BusinessException 如果更新失败
     */
    @CachePut(value = USER_VO_CACHE_PREFIX, key = "#id")
    public void update(String id, User user) {
        user.setId(id);
        if (StringUtils.isNotBlank(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (!this.updateById(user)) {
            throw new BusinessException(UserErrorConstants.USER_NOT_EXISTS, "用户更新失败");
        }
    }

    /**
     * 删除用户。
     *
     * @param id 用户 ID
     * @throws BusinessException 如果删除失败
     */
    public void delete(String id) {
        if (!this.removeById(id)) {
            throw new BusinessException(UserErrorConstants.USER_NOT_EXISTS, "用户删除失败");
        }
    }

    /**
     * 注册新用户。
     *
     * @param user 用户实体
     * @throws BusinessException 如果注册失败
     */
    public void register(User user) {
        checkEmailRegistered(user.getEmail());
        checkPhoneRegistered(user.getPhone());
        checkUsernameOccupied(user.getUsername());
        checkPartyMemberId(String.valueOf(user.getPartyMemberId()));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (!this.save(user)) {
            throw new BusinessException(UserErrorConstants.USER_NOT_EXISTS, "用户注册失败");
        }
    }

    /**
     * 修改当前用户密码。
     *
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     * @throws BusinessException 如果修改失败
     */
    public void changePassword(String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.isEmpty()) {
            throw new BusinessException(UserErrorConstants.EMPTY_ARGS, "有空参数");
        }
        User user = getCurrentUser();
        if (passwordEncoder.matches(oldPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            if (!this.updateById(user)) {
                throw new BusinessException(UserErrorConstants.PASSWORD_CHANGE_ERROR, "修改密码时出现错误");
            }
        } else {
            throw new BusinessException(UserErrorConstants.PASSWORD_CHANGE_ERROR, "修改密码时出现错误");
        }
    }

    /**
     * @param id 用户ID
     * @param schoolId 学校id
     * @return 是否属于该学校
     */
    public Boolean isUserBelongsSchool(String id, String schoolId) {
        if (schoolId == null) {
            throw new BusinessException(UserErrorConstants.EMPTY_ARGS, "有空参数");
        }
        User targetUser = this.getById(id);
        return targetUser != null
                && targetUser.getUniversityId() != null
                && targetUser.getUniversityId().equals(schoolId);
    }

    /**
     * 按条件分页查询用户（用户侧，仅返回公开信息）。
     *
     * @param dto 查询条件
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 用户公开信息分页结果
     */
    public Page<UserPublicResponse> getPage(UserRequest request, int pageNum, int pageSize) {
        Page<User> pageInfo = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = buildQueryWrapper(request);
        Page<User> result = this.page(pageInfo, wrapper);
        Page<UserPublicResponse> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(convertor.toPublicResponse(result.getRecords()));
        return voPage;
    }

    /**
     * 按条件分页查询用户（管理员侧，返回完整信息）。
     *
     * @param dto 查询条件
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 用户完整信息分页结果
     */
    public Page<User> getAdminPage(UserRequest request, int pageNum, int pageSize) {
        Page<User> pageInfo = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = buildQueryWrapper(request);
        return this.page(pageInfo, wrapper);
    }

    private LambdaQueryWrapper<User> buildQueryWrapper(UserRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(request.getUserId()), User::getId, request.getUserId())
                .like(StringUtils.isNotBlank(request.getUsername()), User::getUsername, request.getUsername())
                .like(StringUtils.isNotBlank(request.getRealName()), User::getRealName, request.getRealName())
                .like(
                        StringUtils.isNotBlank(request.getPartyMemberId()),
                        User::getPartyMemberId,
                        request.getPartyMemberId())
                .like(StringUtils.isNotBlank(request.getEmail()), User::getEmail, request.getEmail())
                .like(StringUtils.isNotBlank(request.getPhone()), User::getPhone, request.getPhone())
                .eq(request.getUserType() != null, User::getUserType, request.getUserType())
                .eq(request.getPartyStatus() != null, User::getPartyStatus, request.getPartyStatus())
                .eq(StringUtils.isNotBlank(request.getUniversityId()), User::getUniversityId, request.getUniversityId())
                .like(StringUtils.isNotBlank(request.getBranchName()), User::getBranchName, request.getBranchName());
        return wrapper;
    }

    /**
     * 校验邮箱是否已注册。
     *
     * @param email 邮箱
     */
    private void checkEmailRegistered(String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>().eq(User::getEmail, email);
        if (this.exists(queryWrapper)) {
            throw new BusinessException(UserErrorConstants.EMAIL_EXISTS, "该邮箱已被注册");
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
            throw new BusinessException(UserErrorConstants.PHONE_EXISTS, "该手机号已被注册");
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
            throw new BusinessException(UserErrorConstants.USERNAME_EXISTS, "该昵称已被占用");
        }
    }

    /**
     * 校验党员编号是否已存在。
     *
     * @param partyMemberId 党员编号
     */
    private void checkPartyMemberId(String partyMemberId) {
        LambdaQueryWrapper<User> queryWrapper =
                new LambdaQueryWrapper<User>().eq(User::getPartyMemberId, partyMemberId);
        if (this.exists(queryWrapper)) {
            throw new BusinessException(UserErrorConstants.PARTY_MEMBER_ID_EXISTS, "党员编号已存在");
        }
    }
}
