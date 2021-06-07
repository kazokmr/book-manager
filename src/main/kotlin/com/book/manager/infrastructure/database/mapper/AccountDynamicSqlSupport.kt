/*
 * Auto-generated file. Created by MyBatis Generator
 */
package com.book.manager.infrastructure.database.mapper

import com.book.manager.domain.enum.RoleType
import java.sql.JDBCType
import org.mybatis.dynamic.sql.SqlTable

object AccountDynamicSqlSupport {
    object Account : SqlTable("public.account") {
        val id = column<Long>("id", JDBCType.BIGINT)

        val email = column<String>("email", JDBCType.VARCHAR)

        val password = column<String>("password", JDBCType.VARCHAR)

        val name = column<String>("name", JDBCType.VARCHAR)

        val roleType = column<RoleType>("role_type", JDBCType.VARCHAR, "org.apache.ibatis.type.EnumTypeHandler")
    }
}