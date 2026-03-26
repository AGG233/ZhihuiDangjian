package com.rauio.smartdangjian.server.ai.service.support;

import com.rauio.smartdangjian.server.user.pojo.entity.User;
import com.rauio.smartdangjian.server.user.pojo.vo.UserVO;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof User user) {
            return user;
        }
        return null;
    }

    public String getCurrentUserId() {
        User user = getCurrentUser();
        return user == null ? null : user.getId();
    }

    public UserVO getCurrentUserView() {
        User user = getCurrentUser();
        if (user == null) {
            return null;
        }

        UserVO userVO = new UserVO();
        userVO.setId(user.getId());
        userVO.setUsername(user.getUsername());
        userVO.setRealName(user.getRealName());
        userVO.setParty_member_id(user.getPartyMemberId());
        userVO.setParty_status(user.getPartyStatus());
        userVO.setBranch_name(user.getBranchName());
        userVO.setEmail(user.getEmail());
        userVO.setPhone(user.getPhone());
        return userVO;
    }
}
