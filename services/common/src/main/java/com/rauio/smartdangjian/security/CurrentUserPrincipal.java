package com.rauio.smartdangjian.security;

import com.rauio.smartdangjian.utils.spec.UserType;

public interface CurrentUserPrincipal {

    String getId();

    UserType getUserType();
}
