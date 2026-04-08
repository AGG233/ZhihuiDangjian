package com.rauio.smartdangjian.server.user.pojo.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.rauio.smartdangjian.security.CurrentUserPrincipal;
import com.rauio.smartdangjian.server.user.utils.spec.AccountStatus;
import com.rauio.smartdangjian.server.user.utils.spec.PartyStatus;
import com.rauio.smartdangjian.utils.spec.UserType;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "用户")
public class User implements UserDetails, CurrentUserPrincipal {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    @Schema(description = "用户ID")
    private String id;
    @Schema(description = "学校ID")
    private String universityId;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "密码")
    private String password;
    @Schema(description = "真实姓名")
    private String realName;
    @Schema(description = "身份证号")
    private String idCard;
    @Schema(description = "党员编号")
    private String partyMemberId;
    @Schema(description = "入党时间")
    private LocalDateTime   joinPartyDate;
    @Schema(description = "政治面貌：正式党员、预备党员、发展对象、积极分子、群众")
    private PartyStatus partyStatus;
    @Schema(description = "党支部名称")
    private String branchName;
    @Schema(description = "用户类型：学生、学校、管理员")
    private UserType userType;
    @Schema(description = "账号状态：active表示正常，inactive表示未激活，banned表示封禁")
    private AccountStatus status;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "手机号")
    private String phone;

    @TableField(fill = FieldFill.INSERT)
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String role = "ROLE_" + userType.name();
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }
}
