package com.rauio.ZhihuiDangjian.service;

import com.rauio.ZhihuiDangjian.pojo.User;
import com.rauio.ZhihuiDangjian.pojo.vo.UserVO;

public interface UserService {

    UserVO      getUserByID(String ID);
    User        getUserByName(String username);
    User        getUserByEmail(String email);
    User        getUserByPhone(String phone);
    User        getUserByPartyMemberId(String partyMemberId);
    User        getUserByAll(String username);
    User        getUserFromAuthentication();

    Boolean     update(String id,User user);
    Boolean     delete(String id);
    Boolean     register(User user);
    Boolean     changePassword(String id, String password);
    Boolean     isUserBelongsToTheSchool(String id, String schoolId);


}
