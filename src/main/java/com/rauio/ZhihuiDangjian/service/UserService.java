package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.pojo.vo.UserVO;

public interface UserService {

    UserVO      getUserByID(Long ID);
    User        getUserByName(String username);
    User        getUserByEmail(String email);
    User        getUserByPhone(String phone);
    User        getUserByPartyMemberId(String partyMemberId);
    User        getUserByAll(String username);
    User        getUserFromAuthentication();

    Boolean     update(Long id,User user);
    Boolean     delete(Long id);
    Boolean     register(User user);
    Boolean     changePassword(Long id, String password);
    Boolean     isUserBelongsToTheSchool(Long id, String schoolId);


    Page<User> getUser(UserDto userDto, int pageNum, int pageSize);
}