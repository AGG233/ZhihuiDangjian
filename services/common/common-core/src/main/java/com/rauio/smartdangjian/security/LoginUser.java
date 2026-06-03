package com.rauio.smartdangjian.security;

import com.rauio.smartdangjian.utils.spec.UserType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser {

    private String id;

    private UserType userType;

    private String role;

    private String universityId;
}
