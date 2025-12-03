package com.rauio.ZhihuiDangjian.pojo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HistoryItem {
    private String              id;
    private Integer              type;
    private String              user_id;
    private LocalDateTime                executed_at;
}
