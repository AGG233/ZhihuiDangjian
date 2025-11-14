package com.rauio.ZhihuiDangjiang.pojo.vo;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.rauio.ZhihuiDangjiang.utils.Spec.BlockType;
import com.rauio.ZhihuiDangjiang.utils.Spec.ParentType;

import java.util.Date;

public class ContentBlockVO {
    private String      parentId;
    private ParentType  parentType;
    private BlockType   blockType;
    private String      textContent;
    private String      resourceId;
    private String      caption;
}
