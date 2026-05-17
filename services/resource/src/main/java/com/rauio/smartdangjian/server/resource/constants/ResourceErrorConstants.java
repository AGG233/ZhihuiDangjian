package com.rauio.smartdangjian.server.resource.constants;

/**
 * 资源模块错误码常量（范围 5000-5999）
 */
public class ResourceErrorConstants {

    // 资源元数据
    public static final int RESOURCE_CREATE_FAILED = 5001;
    public static final int RESOURCE_NOT_FOUND = 5002;
    public static final int RESOURCE_UPDATE_FAILED = 5003;
    public static final int RESOURCE_DELETE_FAILED = 5004;
    public static final int RESOURCE_HASH_EXISTS = 5005;
    public static final int RESOURCE_OBJECT_KEY_EXISTS = 5006;

    // 轮播图
    public static final int BANNER_NOT_FOUND = 5011;
    public static final int BANNER_RESOURCE_EMPTY = 5012;
    public static final int BANNER_MAX_SIZE = 5013;
    public static final int BANNER_ALREADY_EXISTS = 5014;
    public static final int BANNER_CREATE_FAILED = 5015;
    public static final int BANNER_ID_AND_HASH_EMPTY = 5016;
}
