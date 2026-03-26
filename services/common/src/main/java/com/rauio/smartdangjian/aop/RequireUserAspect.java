package com.rauio.smartdangjian.aop;

import com.rauio.smartdangjian.aop.annotation.RequireUser;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Aspect
public class RequireUserAspect {

    @Before("@annotation(requireUser) || @within(requireUser)")
    public void requireUser(RequireUser requireUser) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(ErrorConstants.USER_NOT_EXISTS, "用户不存在");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CurrentUserPrincipal)) {
            throw new BusinessException(ErrorConstants.USER_NOT_EXISTS, "用户不存在");
        }
    }
}
