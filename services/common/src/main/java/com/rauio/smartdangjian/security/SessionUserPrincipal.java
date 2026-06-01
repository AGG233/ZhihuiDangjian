package com.rauio.smartdangjian.security;

import java.io.Serial;
import java.io.Serializable;

import com.rauio.smartdangjian.utils.spec.UserType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionUserPrincipal implements CurrentUserPrincipal, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;

    private UserType userType;

    private String universityId;
}
