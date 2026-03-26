package com.rauio.smartdangjian.server.user.pojo.dto;

import com.rauio.smartdangjian.utils.spec.UserType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrentUserDto {
    private String id;
    private UserType userType;
    private String universityId;
}
