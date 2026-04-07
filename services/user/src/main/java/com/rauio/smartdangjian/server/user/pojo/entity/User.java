package com.rauio.smartdangjian.server.user.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.server.user.utils.spec.UserStatus;
import com.rauio.smartdangjian.utils.spec.UserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

@Data
@Builder
@TableName("user")
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails, CurrentUserPrincipal {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private String id;
    private String universityId;
    private String username;
    private String password;
    private String realName;
    private String idCard;
    private String partyMemberId;
    private LocalDateTime   joinPartyDate;
    private UserStatus partyStatus;
    private String branchName;
    private UserType userType;
    private String status;
    private String email;
    private String phone;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = "ROLE_" + userType.name();
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }
}
