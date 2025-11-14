package com.rauio.ZhihuiDangjiang.pojo.vo;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
@Builder
public class PageVO<T>{
    private Long total;
    private Long size;
    private Long current;
    private List<T> list;
}
