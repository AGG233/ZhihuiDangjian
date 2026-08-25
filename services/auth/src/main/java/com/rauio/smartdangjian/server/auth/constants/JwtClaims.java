package com.rauio.smartdangjian.server.auth.constants;

/**
 * 无状态 JWT 的 extra claims 键名定义（签发与鉴权的唯一约定点）。
 *
 * <p>登录时由 {@code AuthService} 写入，鉴权侧
 * （{@code SaTokenPermissionImpl}、{@code SecurityUtils}）按同名读取。
 */
public final class JwtClaims {

    /** 用户角色（UserType 枚举名），RBAC 角色层级展开的依据 */
    public static final String ROLE = "role";

    /** 所属高校 ID，数据范围隔离（@DataScopeAccess）依赖 */
    public static final String UNIVERSITY_ID = "uni";

    /** 令牌版本号，改密/封禁时递增使存量令牌全端失效 */
    public static final String TOKEN_VERSION = "ver";

    private JwtClaims() {}
}
