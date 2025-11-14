package com.rauio.ZhihuiDangjian.pojo;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.baomidou.mybatisplus.annotation.TableName;
import com.rauio.ZhihuiDangjian.utils.Spec.BlockType;
import com.rauio.ZhihuiDangjian.utils.Spec.ParentType;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.Date;

@Data
@Builder
@ToString
@TableName("content_block")
public class ContentBlock {
    private String      id;
    private String      parentId;
    @EnumValue
    private ParentType parentType;
    @EnumValue
    private BlockType   blockType;
    private String      textContent;
    private String      resourceId;
    private String      caption;
    private Date        createdAt;
    private Date        updatedAt;
}
