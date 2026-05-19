package com.rauio.smartdangjian.server.resource.aop;

import org.springframework.stereotype.Component;

import com.rauio.smartdangjian.aop.resolver.ResourceOwnerResolver;
import com.rauio.smartdangjian.server.resource.pojo.entity.ResourceMeta;
import com.rauio.smartdangjian.server.resource.service.ResourceMetaService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ResourceMetaOwnerResolver implements ResourceOwnerResolver {

    public static final String RESOURCE_TYPE = "RESOURCE_META";

    private final ResourceMetaService resourceMetaService;

    @Override
    public boolean supports(String resourceType) {
        return RESOURCE_TYPE.equals(resourceType);
    }

    @Override
    public String findResourceOwner(Object resourceId) {
        ResourceMeta meta = resourceMetaService.get(String.valueOf(resourceId));
        return meta.getUploaderId();
    }
}
