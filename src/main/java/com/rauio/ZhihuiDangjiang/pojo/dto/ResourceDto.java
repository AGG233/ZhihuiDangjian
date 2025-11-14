package com.rauio.ZhihuiDangjiang.pojo.dto;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@ToString
@Builder
public class ResourceDto {

    private String  id;
    private String  originalFilename;
    private String  mimeType;
    private String  path;
    private Long    fileSizeBytes;
}
