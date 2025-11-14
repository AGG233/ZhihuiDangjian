package com.rauio.ZhihuiDangjiang.pojo.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
@ToString
public class CosStsDto {
    public String tmpSecretId;
    public String tmpSecretKey;
    public String sessionToken;
    public String token;
}
