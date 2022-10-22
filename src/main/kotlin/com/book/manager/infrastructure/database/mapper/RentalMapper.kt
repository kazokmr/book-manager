/*
 * Auto-generated file. Created by MyBatis Generator
 */
package com.book.manager.infrastructure.database.mapper

import com.book.manager.infrastructure.database.mapper.RentalDynamicSqlSupport.accountId
import com.book.manager.infrastructure.database.mapper.RentalDynamicSqlSupport.bookId
import com.book.manager.infrastructure.database.mapper.RentalDynamicSqlSupport.rental
import com.book.manager.infrastructure.database.mapper.RentalDynamicSqlSupport.rentalDatetime
import com.book.manager.infrastructure.database.mapper.RentalDynamicSqlSupport.returnDeadline
import com.book.manager.infrastructure.database.record.Rental
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Result
import org.apache.ibatis.annotations.ResultMap
import org.apache.ibatis.annotations.Results
import org.apache.ibatis.annotations.SelectProvider
import org.apache.ibatis.type.JdbcType
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.util.SqlProviderAdapter
import org.mybatis.dynamic.sql.util.kotlin.CountCompleter
import org.mybatis.dynamic.sql.util.kotlin.DeleteCompleter
import org.mybatis.dynamic.sql.util.kotlin.KotlinUpdateBuilder
import org.mybatis.dynamic.sql.util.kotlin.SelectCompleter
import org.mybatis.dynamic.sql.util.kotlin.UpdateCompleter
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.countFrom
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.deleteFrom
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.insert
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.insertMultiple
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.selectDistinct
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.selectList
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.selectOne
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.update
import org.mybatis.dynamic.sql.util.mybatis3.CommonCountMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonDeleteMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonInsertMapper
import org.mybatis.dynamic.sql.util.mybatis3.CommonUpdateMapper

@Mapper
interface RentalMapper : CommonCountMapper, CommonDeleteMapper, CommonInsertMapper<Rental>, CommonUpdateMapper {
    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @Results(id="RentalResult", value = [
        Result(column="book_id", property="bookId", jdbcType=JdbcType.BIGINT, id=true),
        Result(column="account_id", property="accountId", jdbcType=JdbcType.BIGINT),
        Result(column="rental_datetime", property="rentalDatetime", jdbcType=JdbcType.TIMESTAMP),
        Result(column="return_deadline", property="returnDeadline", jdbcType=JdbcType.TIMESTAMP)
    ])
    fun selectMany(selectStatement: SelectStatementProvider): List<Rental>

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @ResultMap("RentalResult")
    fun selectOne(selectStatement: SelectStatementProvider): Rental?
}

fun RentalMapper.count(completer: CountCompleter) =
    countFrom(this::count, rental, completer)

fun RentalMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, rental, completer)

fun RentalMapper.deleteByPrimaryKey(bookId_: Long) =
    delete {
        where { bookId isEqualTo bookId_ }
    }

fun RentalMapper.insert(row: Rental) =
    insert(this::insert, row, rental) {
        map(bookId) toProperty "bookId"
        map(accountId) toProperty "accountId"
        map(rentalDatetime) toProperty "rentalDatetime"
        map(returnDeadline) toProperty "returnDeadline"
    }

fun RentalMapper.insertMultiple(records: Collection<Rental>) =
    insertMultiple(this::insertMultiple, records, rental) {
        map(bookId) toProperty "bookId"
        map(accountId) toProperty "accountId"
        map(rentalDatetime) toProperty "rentalDatetime"
        map(returnDeadline) toProperty "returnDeadline"
    }

fun RentalMapper.insertMultiple(vararg records: Rental) =
    insertMultiple(records.toList())

fun RentalMapper.insertSelective(row: Rental) =
    insert(this::insert, row, rental) {
        map(bookId).toPropertyWhenPresent("bookId", row::bookId)
        map(accountId).toPropertyWhenPresent("accountId", row::accountId)
        map(rentalDatetime).toPropertyWhenPresent("rentalDatetime", row::rentalDatetime)
        map(returnDeadline).toPropertyWhenPresent("returnDeadline", row::returnDeadline)
    }

private val columnList = listOf(bookId, accountId, rentalDatetime, returnDeadline)

fun RentalMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, rental, completer)

fun RentalMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, rental, completer)

fun RentalMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, rental, completer)

fun RentalMapper.selectByPrimaryKey(bookId_: Long) =
    selectOne {
        where { bookId isEqualTo bookId_ }
    }

fun RentalMapper.update(completer: UpdateCompleter) =
    update(this::update, rental, completer)

fun KotlinUpdateBuilder.updateAllColumns(row: Rental) =
    apply {
        set(bookId) equalToOrNull row::bookId
        set(accountId) equalToOrNull row::accountId
        set(rentalDatetime) equalToOrNull row::rentalDatetime
        set(returnDeadline) equalToOrNull row::returnDeadline
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(row: Rental) =
    apply {
        set(bookId) equalToWhenPresent row::bookId
        set(accountId) equalToWhenPresent row::accountId
        set(rentalDatetime) equalToWhenPresent row::rentalDatetime
        set(returnDeadline) equalToWhenPresent row::returnDeadline
    }

fun RentalMapper.updateByPrimaryKey(row: Rental) =
    update {
        set(accountId) equalToOrNull row::accountId
        set(rentalDatetime) equalToOrNull row::rentalDatetime
        set(returnDeadline) equalToOrNull row::returnDeadline
        where { bookId isEqualTo row.bookId!! }
    }

fun RentalMapper.updateByPrimaryKeySelective(row: Rental) =
    update {
        set(accountId) equalToWhenPresent row::accountId
        set(rentalDatetime) equalToWhenPresent row::rentalDatetime
        set(returnDeadline) equalToWhenPresent row::returnDeadline
        where { bookId isEqualTo row.bookId!! }
    }