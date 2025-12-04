package com.rauio.ZhihuiDangjian.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.rauio.ZhihuiDangjian.utils.Spec.UserStatus;
import com.rauio.ZhihuiDangjian.utils.Spec.UserType;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;

/**
 * (User)实体类
 *
 * @author makejava
 * @since 2024-09-06 11:06:42
 */
@Data
@Builder
@TableName("user")
public class User implements UserDetails {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;
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

    /**
     * Returns the authorities granted to the user. Cannot return <code>null</code>.
     *
     * @return the authorities, sorted by natural key (never <code>null</code>)
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = "ROLE_" + userType.name();
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }
}