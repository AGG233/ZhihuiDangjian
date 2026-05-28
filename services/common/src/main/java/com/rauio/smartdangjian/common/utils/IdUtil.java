package com.rauio.smartdangjian.common.utils;

import com.rauio.smartdangjian.constants.ErrorConstants;
import com.rauio.smartdangjian.exception.BusinessException;

public final class IdUtil {

    private IdUtil() {}

    /**
     * 将字符串 ID 安全转换为 Long，失败时抛 BusinessException。
     *
     * @param idStr 字符串形式的 ID
     * @return 转换后的 Long 值
     */
    public static Long parse(String idStr) {
        try {
            return Long.valueOf(idStr);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorConstants.ARGS_ERROR, "ID格式错误: " + idStr);
        }
    }

    /**
     * 将字符串 ID 安全转换为 Long，失败时返回默认值。
     *
     * @param idStr       字符串形式的 ID
     * @param defaultVal  转换失败时的默认值
     * @return 转换后的 Long 值，或默认值
     */
    public static Long parseOrDefault(String idStr, Long defaultVal) {
        try {
            return Long.valueOf(idStr);
        } catch (NumberFormatException e) {
            return defaultVal;
        }
    }

    /**
     * 将字符串 ID 安全转换为 Long，允许 null 输入。
     *
     * @param idStr 可为 null 的字符串 ID
     * @return 转换后的 Long 值，或 null
     */
    public static Long parseNullable(String idStr) {
        if (idStr == null || idStr.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(idStr);
        } catch (NumberFormatException e) {
            throw new BusinessException(ErrorConstants.ARGS_ERROR, "ID格式错误: " + idStr);
        }
    }
}
