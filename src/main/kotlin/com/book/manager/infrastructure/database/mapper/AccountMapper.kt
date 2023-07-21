/*
 * Auto-generated file. Created by MyBatis Generator
 */
package com.book.manager.infrastructure.database.mapper

import com.book.manager.infrastructure.database.mapper.AccountDynamicSqlSupport.account
import com.book.manager.infrastructure.database.mapper.AccountDynamicSqlSupport.email
import com.book.manager.infrastructure.database.mapper.AccountDynamicSqlSupport.id
import com.book.manager.infrastructure.database.mapper.AccountDynamicSqlSupport.name
import com.book.manager.infrastructure.database.mapper.AccountDynamicSqlSupport.password
import com.book.manager.infrastructure.database.mapper.AccountDynamicSqlSupport.roleType
import com.book.manager.infrastructure.database.record.Account
import com.github.onozaty.mybatis.pg.type.pgenum.PgEnumTypeHandler
import org.apache.ibatis.annotations.*
import org.apache.ibatis.type.JdbcType
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.util.SqlProviderAdapter
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*
import org.mybatis.dynamic.sql.util.mybatis3.CommonCountMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonDeleteMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonInsertMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper

@Mapper
interface AccountMapper : CommonCountMapper, CommonDeleteMapper, CommonInsertMapper<Account>, CommonUpdateMapper {
    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @Results(id="AccountResult", value = [
        Result(column="id", property="id", jdbcType=JdbcType.BIGINT, id=true),
        Result(column="email", property="email", jdbcType=JdbcType.VARCHAR),
        Result(column="password", property="password", jdbcType=JdbcType.VARCHAR),
        Result(column="name", property="name", jdbcType=JdbcType.VARCHAR),
        Result(column="role_type", property="roleType", typeHandler=PgEnumTypeHandler::class, jdbcType=JdbcType.VARCHAR)
    ])
    fun selectMany(selectStatement: SelectStatementProvider): List<Account>

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @ResultMap("AccountResult")
    fun selectOne(selectStatement: SelectStatementProvider): Account?
}

fun AccountMapper.count(completer: CountCompleter) =
    countFrom(this::count, account, completer)

fun AccountMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, account, completer)

fun AccountMapper.deleteByPrimaryKey(id_: Long) =
    delete {
        where { id isEqualTo id_ }
    }

fun AccountMapper.insert(row: Account) =
    insert(this::insert, row, account) {
        map(id) toProperty "id"
        map(email) toProperty "email"
        map(password) toProperty "password"
        map(name) toProperty "name"
        map(roleType) toProperty "roleType"
    }

fun AccountMapper.insertMultiple(records: Collection<Account>) =
    insertMultiple(this::insertMultiple, records, account) {
        map(id) toProperty "id"
        map(email) toProperty "email"
        map(password) toProperty "password"
        map(name) toProperty "name"
        map(roleType) toProperty "roleType"
    }

fun AccountMapper.insertMultiple(vararg records: Account) =
    insertMultiple(records.toList())

fun AccountMapper.insertSelective(row: Account) =
    insert(this::insert, row, account) {
        map(id).toPropertyWhenPresent("id", row::id)
        map(email).toPropertyWhenPresent("email", row::email)
        map(password).toPropertyWhenPresent("password", row::password)
        map(name).toPropertyWhenPresent("name", row::name)
        map(roleType).toPropertyWhenPresent("roleType", row::roleType)
    }

private val columnList = listOf(id, email, password, name, roleType)

fun AccountMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, account, completer)

fun AccountMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, account, completer)

fun AccountMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, account, completer)

fun AccountMapper.selectByPrimaryKey(id_: Long) =
    selectOne {
        where { id isEqualTo id_ }
    }

fun AccountMapper.update(completer: UpdateCompleter) =
    update(this::update, account, completer)

fun KotlinUpdateBuilder.updateAllColumns(row: Account) =
    apply {
        set(id) equalToOrNull row::id
        set(email) equalToOrNull row::email
        set(password) equalToOrNull row::password
        set(name) equalToOrNull row::name
        set(roleType) equalToOrNull row::roleType
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(row: Account) =
    apply {
        set(id) equalToWhenPresent row::id
        set(email) equalToWhenPresent row::email
        set(password) equalToWhenPresent row::password
        set(name) equalToWhenPresent row::name
        set(roleType) equalToWhenPresent row::roleType
    }

fun AccountMapper.updateByPrimaryKey(row: Account) =
    update {
        set(email) equalToOrNull row::email
        set(password) equalToOrNull row::password
        set(name) equalToOrNull row::name
        set(roleType) equalToOrNull row::roleType
        where { id isEqualTo row.id!! }
    }

fun AccountMapper.updateByPrimaryKeySelective(row: Account) =
    update {
        set(email) equalToWhenPresent row::email
        set(password) equalToWhenPresent row::password
        set(name) equalToWhenPresent row::name
        set(roleType) equalToWhenPresent row::roleType
        where { id isEqualTo row.id!! }
    }
