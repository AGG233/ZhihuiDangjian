package com.rauio.smartdangjian.server.content.pojo.vo;

import com.rauio.smartdangjian.server.content.spec.BlockType;
import com.rauio.smartdangjian.server.content.spec.ParentType;

public class ContentBlockVO {
    private String      parentId;
    private ParentType  parentType;
    private BlockType   blockType;
    private String      textContent;
    private String      resourceId;
    private String      caption;
}
