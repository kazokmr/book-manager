/*
 * Auto-generated file. Created by MyBatis Generator
 */
package com.book.manager.infrastructure.database.mapper

import com.book.manager.domain.enum.RoleType
import org.mybatis.dynamic.sql.AliasableSqlTable
import org.mybatis.dynamic.sql.util.kotlin.elements.column
import java.sql.JDBCType

object AccountDynamicSqlSupport {
    val account = Account()

    val id = account.id

    val email = account.email

    val password = account.password

    val name = account.name

    val roleType = account.roleType

    class Account : AliasableSqlTable<Account>("public.account", ::Account) {
        val id = column<Long>(name = "id", jdbcType = JDBCType.BIGINT)

        val email = column<String>(name = "email", jdbcType = JDBCType.VARCHAR)

        val password = column<String>(name = "password", jdbcType = JDBCType.VARCHAR)

        val name = column<String>(name = "name", jdbcType = JDBCType.VARCHAR)

        val roleType = column<RoleType>(name = "role_type", jdbcType = JDBCType.VARCHAR, typeHandler = "com.github.onozaty.mybatis.pg.type.pgenum.PgEnumTypeHandler")
    }
}
