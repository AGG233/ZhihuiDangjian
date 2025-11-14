package com.rauio.ZhihuiDangjian.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class HistoryItem {
    private String              id;
    private Integer              type;
    private String              user_id;
    private Date                executed_at;
}
