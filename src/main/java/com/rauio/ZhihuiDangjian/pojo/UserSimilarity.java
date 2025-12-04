package com.rauio.ZhihuiDangjian.pojo;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@TableName("user_similarity")
public class UserSimilarity {

    @TableId
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private String userId1;

    private String userId2;

    private double similarityScore;

    private String similarityType;

    private String calculationParams;

    private String dataVersion;

    private LocalDateTime calculatedAt;

    private Boolean isValid;

    private LocalDateTime expiresAt;


    public UserSimilarity(String userId1,String userId2,double similarityScore) {
        this.userId1=userId1;
        this.userId2=userId2;
        this.similarityScore=similarityScore;
    }
}