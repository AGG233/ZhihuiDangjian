package com.rauio.smartdangjian.aop.support;

public interface DataScopeResolver {

    boolean supports(String resource);

    void before(DataScopeContext context);

    default Object after(DataScopeContext context, Object result) {
        return result;
    }
}
