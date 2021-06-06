/*
 * Auto-generated file. Created by MyBatis Generator
 */
package com.book.manager.infrastructure.database.mapper

import com.book.manager.infrastructure.database.mapper.AccountDynamicSqlSupport.Account
import com.book.manager.infrastructure.database.mapper.AccountDynamicSqlSupport.Account.email
import com.book.manager.infrastructure.database.mapper.AccountDynamicSqlSupport.Account.id
import com.book.manager.infrastructure.database.mapper.AccountDynamicSqlSupport.Account.name
import com.book.manager.infrastructure.database.mapper.AccountDynamicSqlSupport.Account.password
import com.book.manager.infrastructure.database.mapper.AccountDynamicSqlSupport.Account.roleType
import com.book.manager.infrastructure.database.record.AccountRecord
import org.mybatis.dynamic.sql.SqlBuilder.isEqualTo
import org.mybatis.dynamic.sql.util.kotlin.*
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.*

fun AccountMapper.count(completer: CountCompleter) =
    countFrom(this::count, Account, completer)

fun AccountMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, Account, completer)

fun AccountMapper.deleteByPrimaryKey(id_: Long) =
    delete {
        where(id, isEqualTo(id_))
    }

fun AccountMapper.insert(record: AccountRecord) =
    insert(this::insert, record, Account) {
        map(id).toProperty("id")
        map(email).toProperty("email")
        map(password).toProperty("password")
        map(name).toProperty("name")
        map(roleType).toProperty("roleType")
    }

fun AccountMapper.insertMultiple(records: Collection<AccountRecord>) =
    insertMultiple(this::insertMultiple, records, Account) {
        map(id).toProperty("id")
        map(email).toProperty("email")
        map(password).toProperty("password")
        map(name).toProperty("name")
        map(roleType).toProperty("roleType")
    }

fun AccountMapper.insertMultiple(vararg records: AccountRecord) =
    insertMultiple(records.toList())

fun AccountMapper.insertSelective(record: AccountRecord) =
    insert(this::insert, record, Account) {
        map(id).toPropertyWhenPresent("id", record::id)
        map(email).toPropertyWhenPresent("email", record::email)
        map(password).toPropertyWhenPresent("password", record::password)
        map(name).toPropertyWhenPresent("name", record::name)
        map(roleType).toPropertyWhenPresent("roleType", record::roleType)
    }

private val columnList = listOf(id, email, password, name, roleType)

fun AccountMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, Account, completer)

fun AccountMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, Account, completer)

fun AccountMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, Account, completer)

fun AccountMapper.selectByPrimaryKey(id_: Long) =
    selectOne {
        where(id, isEqualTo(id_))
    }

fun AccountMapper.update(completer: UpdateCompleter) =
    update(this::update, Account, completer)

fun KotlinUpdateBuilder.updateAllColumns(record: AccountRecord) =
    apply {
        set(id).equalTo(record::id)
        set(email).equalTo(record::email)
        set(password).equalTo(record::password)
        set(name).equalTo(record::name)
        set(roleType).equalTo(record::roleType)
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(record: AccountRecord) =
    apply {
        set(id).equalToWhenPresent(record::id)
        set(email).equalToWhenPresent(record::email)
        set(password).equalToWhenPresent(record::password)
        set(name).equalToWhenPresent(record::name)
        set(roleType).equalToWhenPresent(record::roleType)
    }

fun AccountMapper.updateByPrimaryKey(record: AccountRecord) =
    update {
        set(email).equalTo(record::email)
        set(password).equalTo(record::password)
        set(name).equalTo(record::name)
        set(roleType).equalTo(record::roleType)
        where(id, isEqualTo(record::id))
    }

fun AccountMapper.updateByPrimaryKeySelective(record: AccountRecord) =
    update {
        set(email).equalToWhenPresent(record::email)
        set(password).equalToWhenPresent(record::password)
        set(name).equalToWhenPresent(record::name)
        set(roleType).equalToWhenPresent(record::roleType)
        where(id, isEqualTo(record::id))
    }