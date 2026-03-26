package com.rauio.smartdangjian.server.resource.pojo.conventor;


import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ResourceMetaConvertor {
}
