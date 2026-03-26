package com.rauio.smartdangjian.aop.resolver;

public interface ResourceOwnerResolver {

    boolean supports(String resourceType);

    String findResourceOwner(Object resourceId);
}
