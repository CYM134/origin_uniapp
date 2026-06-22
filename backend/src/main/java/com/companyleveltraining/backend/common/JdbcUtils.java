package com.companyleveltraining.backend.common;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * JDBC 取值工具。
 * 注意：MySQL 的 BIGINT UNSIGNED 经驱动返回为 {@link java.math.BigInteger}，
 * 直接 {@code (Long) rs.getObject(col)} 会抛 ClassCastException，须用 getLong + wasNull。
 */
public final class JdbcUtils {

    private JdbcUtils() {
    }

    /** 读取可空的 BIGINT 列为 Long（兼容 BIGINT UNSIGNED）。 */
    public static Long nullableLong(ResultSet rs, String column) throws SQLException {
        long value = rs.getLong(column);
        return rs.wasNull() ? null : value;
    }
}
