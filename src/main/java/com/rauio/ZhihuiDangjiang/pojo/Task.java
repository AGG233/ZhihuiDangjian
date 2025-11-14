package com.rauio.ZhihuiDangjiang.pojo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class Task {

    private String          name;
    private LocalDateTime   startTime;
}
