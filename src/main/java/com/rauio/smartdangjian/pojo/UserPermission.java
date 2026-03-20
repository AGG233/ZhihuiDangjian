package com.rauio.smartdangjian.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.rauio.smartdangjian.utils.spec.UserType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("user_permission")
public class UserPermission {

    @TableId(type = IdType.AUTO)
    private String userId;
    private UserType userType;
    private Integer level;

}
