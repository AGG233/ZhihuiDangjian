package com.rauio.ZhihuiDangjiang.pojo;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@TableName("universities")
public class Universities {
    private String    id;
    private String  name;
}