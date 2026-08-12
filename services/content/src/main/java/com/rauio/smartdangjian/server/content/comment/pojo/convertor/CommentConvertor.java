package com.rauio.smartdangjian.server.content.comment.pojo.convertor;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.rauio.smartdangjian.server.content.comment.pojo.entity.Comment;
import com.rauio.smartdangjian.server.content.comment.pojo.response.CommentResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CommentConvertor {

    CommentResponse toResponse(Comment entity);

    List<CommentResponse> toResponseList(List<Comment> entities);
}
