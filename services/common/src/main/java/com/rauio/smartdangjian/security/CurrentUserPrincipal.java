package com.rauio.smartdangjian.security;

import com.rauio.smartdangjian.utils.spec.UserType;

public interface CurrentUserPrincipal {

    Long getId();

    UserType getUserType();

    String getUniversityId();
}
