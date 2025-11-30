package com.rauio.ZhihuiDangjian.pojo.dto;

import com.rauio.ZhihuiDangjian.utils.Spec.BlockType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;


@Data
@Builder

@Schema(description = "内容块请求体")
public class ContentBlockDto {

    @Schema(description = "内容块类型，有如下字段：'heading','paragraph','image','video','attachment'，heading和paragraph为文本内容，其他的均为文件")
    private BlockType   blockType;

    @Schema(description = "如果内容块类型为文本类型，这是个字段就是存文本的；如果是文件类型也可以填这个字段")
    private String      textContent;

    @Schema(description = "文件的资源id，如果是纯文本内容请忽略")
    private String      resourceId;

    @Schema(description = "说明字段，这是个备用字段")
    private String      caption;
}
