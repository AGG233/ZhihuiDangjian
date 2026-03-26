package com.rauio.smartdangjian.server.content.pojo.request;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class PageRequest {
    private int pageNum;
    private int pageSize;
}
