package com.rauio.ZhihuiDangjian.aop.resolver;

public interface ResourceOwnerResolver {

    boolean supports(String resourceType);

    String findResourceOwner(Object resourceId);
}
