package com.rauio.smartdangjian.server.user.service;

import static com.rauio.smartdangjian.constants.RedisConstants.USER_VO_CACHE_PREFIX;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserProvider;
import com.rauio.smartdangjian.server.user.constants.UserErrorConstants;
import com.rauio.smartdangjian.server.user.mapper.UserMapper;
import com.rauio.smartdangjian.server.user.pojo.convertor.UserConvertor;
import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.pojo.request.UserRequest;
import com.rauio.smartdangjian.server.user.pojo.request.UserUpdateRequest;
import com.rauio.smartdangjian.server.user.pojo.response.UserPublicResponse;
import com.rauio.smartdangjian.server.user.pojo.response.UserResponse;

import cn.hutool.crypto.digest.BCrypt;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService extends ServiceImpl<UserMapper, User> {

    private final UserConvertor convertor;
    private final CurrentUserProvider currentUserProvider;

    /**
     * 根据通行凭证识别并查询用户。
     *
     * @param passport 用户名、邮箱或手机号
     * @return 用户实体
     * @throws BusinessException 如果通行凭证为空
     */
    @Cacheable(value = USER_VO_CACHE_PREFIX, key = "#passport", sync = true)
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
    @Cacheable(value = USER_VO_CACHE_PREFIX, key = "#id", sync = true)
    public UserResponse get(Long id) {
        return convertor.toResponse(this.getById(id));
    }

    /**
     * 获取当前登录用户。
     *
     * @return 当前调用接口的用户
     */
    public User getCurrentUser() {
        String userId = getCurrentUserId();
        if (userId == null) {
            return null;
        }
        return this.getById(userId);
    }

    /**
     * 获取当前登录用户 ID。
     *
     * @return 当前用户 ID，未登录时返回开发环境默认值（如有配置）
     */
    public String getCurrentUserId() {
        String currentUserId = currentUserProvider.getCurrentUserId();
        if (currentUserId == null || currentUserId.isEmpty()) {
            return null;
        }
        return currentUserId;
    }

    /**
     * 根据用户名查询用户。
     *
     * @param username 用户名
     * @return 用户实体
     */
    @Cacheable(value = USER_VO_CACHE_PREFIX, key = "#username", sync = true)
    public User getByUsername(String username) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    /**
     * 根据邮箱查询用户。
     *
     * @param email 邮箱
     * @return 用户实体
     */
    @Cacheable(value = USER_VO_CACHE_PREFIX, key = "#email", sync = true)
    public User getByEmail(String email) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getEmail, email));
    }

    /**
     * 根据手机号查询用户。
     *
     * @param phone 手机号
     * @return 用户实体
     */
    @Cacheable(value = USER_VO_CACHE_PREFIX, key = "#phone", sync = true)
    public User getByPhone(String phone) {
        return this.getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }

    /**
     * 根据党员编号查询用户。
     *
     * @param partyMemberId 党员编号
     * @return 用户实体
     */
    @Cacheable(value = USER_VO_CACHE_PREFIX, key = "#partyMemberId", sync = true)
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
    @Caching(
            evict = {
                @CacheEvict(value = USER_VO_CACHE_PREFIX, key = "#id"),
                @CacheEvict(value = USER_VO_CACHE_PREFIX, key = "#user.username", condition = "#user.username != null"),
                @CacheEvict(value = USER_VO_CACHE_PREFIX, key = "#user.email", condition = "#user.email != null"),
                @CacheEvict(value = USER_VO_CACHE_PREFIX, key = "#user.phone", condition = "#user.phone != null"),
                @CacheEvict(
                        value = USER_VO_CACHE_PREFIX,
                        key = "#user.partyMemberId",
                        condition = "#user.partyMemberId != null")
            })
    public User update(Long id, User user) {
        user.setId(id);
        if (StringUtils.isNotBlank(user.getPassword())) {
            user.setPassword(BCrypt.hashpw(user.getPassword()));
        }
        if (!this.updateById(user)) {
            throw new BusinessException(UserErrorConstants.USER_NOT_EXISTS, "用户更新失败");
        }
        return this.getById(id);
    }

    @Caching(
            evict = {
                @CacheEvict(value = USER_VO_CACHE_PREFIX, key = "#id"),
                @CacheEvict(value = USER_VO_CACHE_PREFIX, key = "#request.email", condition = "#request.email != null"),
                @CacheEvict(value = USER_VO_CACHE_PREFIX, key = "#request.phone", condition = "#request.phone != null"),
                @CacheEvict(
                        value = USER_VO_CACHE_PREFIX,
                        key = "#request.partyMemberId",
                        condition = "#request.partyMemberId != null")
            })
    public User update(Long id, UserUpdateRequest request) {
        User user = convertor.toEntity(request);
        return update(id, user);
    }

    @Caching(
            evict = {
                @CacheEvict(value = USER_VO_CACHE_PREFIX, key = "#id"),
                @CacheEvict(
                        value = USER_VO_CACHE_PREFIX,
                        key = "#request.username",
                        condition = "#request.username != null"),
                @CacheEvict(value = USER_VO_CACHE_PREFIX, key = "#request.email", condition = "#request.email != null"),
                @CacheEvict(value = USER_VO_CACHE_PREFIX, key = "#request.phone", condition = "#request.phone != null"),
                @CacheEvict(
                        value = USER_VO_CACHE_PREFIX,
                        key = "#request.partyMemberId",
                        condition = "#request.partyMemberId != null")
            })
    public User update(Long id, UserRequest request) {
        User user = convertor.toEntity(request);
        return update(id, user);
    }

    /**
     * 删除用户。
     *
     * @param id 用户 ID
     * @throws BusinessException 如果删除失败
     */
    @CacheEvict(value = USER_VO_CACHE_PREFIX, key = "#id")
    public void delete(Long id) {
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
    @Caching(
            evict = {
                @CacheEvict(value = USER_VO_CACHE_PREFIX, key = "#user.username", condition = "#user.username != null"),
                @CacheEvict(value = USER_VO_CACHE_PREFIX, key = "#user.email", condition = "#user.email != null"),
                @CacheEvict(value = USER_VO_CACHE_PREFIX, key = "#user.phone", condition = "#user.phone != null"),
                @CacheEvict(
                        value = USER_VO_CACHE_PREFIX,
                        key = "#user.partyMemberId",
                        condition = "#user.partyMemberId != null")
            })
    public void register(User user) {
        if (user == null) {
            throw new BusinessException(UserErrorConstants.EMPTY_ARGS, "有空参数");
        }
        checkEmailRegistered(user.getEmail());
        checkPhoneRegistered(user.getPhone());
        checkUsernameOccupied(user.getUsername());
        checkPartyMemberId(user.getPartyMemberId());
        user.setPassword(BCrypt.hashpw(user.getPassword()));
        if (!this.save(user)) {
            throw new BusinessException(UserErrorConstants.USER_NOT_EXISTS, "用户注册失败");
        }
    }

    @Caching(
            evict = {
                @CacheEvict(
                        value = USER_VO_CACHE_PREFIX,
                        key = "#request.username",
                        condition = "#request.username != null"),
                @CacheEvict(value = USER_VO_CACHE_PREFIX, key = "#request.email", condition = "#request.email != null"),
                @CacheEvict(value = USER_VO_CACHE_PREFIX, key = "#request.phone", condition = "#request.phone != null"),
                @CacheEvict(
                        value = USER_VO_CACHE_PREFIX,
                        key = "#request.partyMemberId",
                        condition = "#request.partyMemberId != null")
            })
    public void register(UserRequest request) {
        register(convertor.toEntity(request));
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
        String currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(UserErrorConstants.PASSWORD_CHANGE_ERROR, "未登录或登录已过期");
        }
        changePasswordForUser(currentUserId, oldPassword, newPassword);
    }

    @CacheEvict(value = USER_VO_CACHE_PREFIX, key = "#userId")
    public void changePasswordForUser(String userId, String oldPassword, String newPassword) {
        if (oldPassword == null || oldPassword.isEmpty()) {
            throw new BusinessException(UserErrorConstants.EMPTY_ARGS, "有空参数");
        }
        User user = this.getById(userId);
        if (user == null) {
            throw new BusinessException(UserErrorConstants.USER_NOT_EXISTS, "用户不存在");
        }
        if (BCrypt.checkpw(oldPassword, user.getPassword())) {
            user.setPassword(BCrypt.hashpw(newPassword));
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
    public Boolean isUserBelongsSchool(Long id, String schoolId) {
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
     * @param request 查询条件
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 用户完整信息分页结果
     */
    public Page<User> getAdminPage(UserRequest request, int pageNum, int pageSize) {
        Page<User> pageInfo = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = buildAdminQueryWrapper(request);
        return this.page(pageInfo, wrapper);
    }

    public Page<UserResponse> getAdminResponsePage(UserRequest request, int pageNum, int pageSize) {
        Page<User> result = getAdminPage(request, pageNum, pageSize);
        Page<UserResponse> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(convertor.toResponse(result.getRecords()));
        return responsePage;
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

    private LambdaQueryWrapper<User> buildAdminQueryWrapper(UserRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.isNotBlank(request.getUsername()), User::getUsername, request.getUsername())
                .like(StringUtils.isNotBlank(request.getRealName()), User::getRealName, request.getRealName())
                .like(
                        StringUtils.isNotBlank(request.getPartyMemberId()),
                        User::getPartyMemberId,
                        request.getPartyMemberId())
                .eq(StringUtils.isNotBlank(request.getEmail()), User::getEmail, request.getEmail())
                .eq(StringUtils.isNotBlank(request.getPhone()), User::getPhone, request.getPhone())
                .eq(request.getUserType() != null, User::getUserType, request.getUserType())
                .eq(request.getPartyStatus() != null, User::getPartyStatus, request.getPartyStatus())
                .eq(StringUtils.isNotBlank(request.getUniversityId()), User::getUniversityId, request.getUniversityId())
                .like(StringUtils.isNotBlank(request.getBranchName()), User::getBranchName, request.getBranchName());
        return wrapper;
    }
}
