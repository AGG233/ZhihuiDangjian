package com.rauio.smartdangjian.server.quiz.client.dto;

import com.rauio.smartdangjian.utils.spec.UserType;
import lombok.Data;

@Data
public class CurrentUserDto {
    private String id;
    private UserType userType;
    private String universityId;
}
