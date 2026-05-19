package com.rauio.smartdangjian.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.rauio.smartdangjian.aop.annotation.RequireUser;
import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;

@Aspect
public class RequireUserAspect {

    @Before("@annotation(requireUser) || @within(requireUser)")
    public void requireUser(RequireUser requireUser) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new BusinessException(ErrorConstants.USER_NOT_EXISTS, "用户不存在");
        }

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof CurrentUserPrincipal)) {
            throw new BusinessException(ErrorConstants.USER_NOT_EXISTS, "用户不存在");
        }
    }
}
