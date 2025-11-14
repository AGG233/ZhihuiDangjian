package com.rauio.ZhihuiDangjiang.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@TableName("resource_meta")
public class ResourceMeta {

    @TableId
    private String id;
    private String uploaderId;
    private String originalName;
    private String hash;
}
