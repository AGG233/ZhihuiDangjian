package com.rauio.smartdangjian.security;

public final class RoleConstants {

    public static final String MANAGER = "MANAGER";
    public static final String SCHOOL = "SCHOOL";
    public static final String STUDENT = "STUDENT";

    private RoleConstants() {}

    /**
     * 比较用户角色是否满足所需角色的层级要求。
     * <p>
     * 角色层级：MANAGER &gt; SCHOOL &gt; STUDENT
     * </p>
     *
     * @param userRole     当前用户角色
     * @param requiredRole 所需角色
     * @return 如果当前用户角色层级大于或等于所需角色层级则返回 true
     */
    public static boolean hasRole(String userRole, String requiredRole) {
        return getRoleLevel(userRole) >= getRoleLevel(requiredRole);
    }

    private static int getRoleLevel(String role) {
        if (MANAGER.equals(role)) {
            return 3;
        }
        if (SCHOOL.equals(role)) {
            return 2;
        }
        if (STUDENT.equals(role)) {
            return 1;
        }
        return 0;
    }
}
