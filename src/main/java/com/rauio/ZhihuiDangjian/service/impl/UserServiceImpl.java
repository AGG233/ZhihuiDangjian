package com.rauio.ZhihuiDangjian.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.rauio.ZhihuiDangjian.dao.UserDao;
import com.rauio.ZhihuiDangjian.exception.BusinessException;
import com.rauio.ZhihuiDangjian.mapper.UserMapper;
import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.pojo.convertor.UserConvertor;
import com.rauio.ZhihuiDangjian.pojo.vo.UserVO;
import com.rauio.ZhihuiDangjian.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final UserDao userDao;
    private final UserConvertor convertor;

    public UserVO getUserByID(Long ID) {
        return convertor.toVO(userDao.get(ID));
    }
    
    public User getUserByAll(String passport){
        if (passport == null || passport.isEmpty()) {
            return null;
        }
        if (passport.contains("@")){
            return getUserByEmail(passport);
        }
        if (passport.contains("+")){
            return getUserByPhone(passport);
        }else{
            return getUserByName(passport);
        }
    }

    /**
     * @return
     */
    @Override
    public User getUserFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof User) {
                return (User) principal;
            }
        }
        return null;
    }

    public User getUserByName(String username) {
        return userDao.getUserByName(username);
    }

    public User getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }

    public User getUserByPhone(String phone) {
        return userDao.getUserByPhone(phone);
    }

    public User getUserByPartyMemberId(String partyMemberId) {
        return userDao.getUserByPartyMemberId(partyMemberId);
    }

    public Boolean update(Long id,User user) {
        return userDao.update(user);
    }

    public Boolean delete(Long ID) {
        return userDao.delete(ID);
    }

    public Boolean register(User user) {
        checkEmailRegistered(user.getEmail());
        checkPhoneRegistered(user.getPhone());
        checkUsernameOccupied(user.getUsername());
        checkPartyMemberId(String.valueOf(user.getPartyMemberId()));
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userDao.insert(user);
    }

    /**
     * @param id
     * @param schoolId
     * @return
     */
    @Override
    public Boolean isUserBelongsToTheSchool(Long id, String schoolId) {
        return null;
    }

    public Boolean changePassword(Long id, String password) {
        return userDao.changePassword(id,password);
    }
    
    private void checkEmailRegistered(String email) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>().eq(User::getEmail, email);
        if (this.userMapper.exists(queryWrapper)) {
            throw new BusinessException(BusinessException.EMAIL_EXISTS,"该邮箱已被注册");
        }
    }
    private void checkPhoneRegistered(String phone) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>().eq(User::getPhone, phone);
        if (this.userMapper.exists(queryWrapper)) {
            throw new BusinessException(BusinessException.PHONE_EXISTS, "该手机号已被注册");
        }
    }
    private void checkUsernameOccupied(String username) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>().eq(User::getUsername, username);
        if (this.userMapper.exists(queryWrapper)) {
            throw new BusinessException(BusinessException.USERNAME_EXISTS, "该昵称已被占用");
        }
    }
    private void checkPartyMemberId(String partyMemberId) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<User>().eq(User::getPartyMemberId, partyMemberId);
        if (this.userMapper.exists(queryWrapper)) {
            throw new BusinessException(BusinessException.PARTY_MEMBER_ID_EXISTS, "党员编号已存在");
        }
    }
}