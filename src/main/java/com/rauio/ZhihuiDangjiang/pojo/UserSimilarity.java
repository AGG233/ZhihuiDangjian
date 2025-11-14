package com.rauio.ZhihuiDangjiang.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@TableName("user_similarity")
public class UserSimilarity {

    @TableId
    private Integer id;

    private Integer userId1;

    private Integer userId2;

    private BigDecimal similarityScore;

    private String similarityType;

    private String calculationParams;

    private String dataVersion;

    private Date calculatedAt;

    private Boolean isValid;

    private Date expiresAt;

}
