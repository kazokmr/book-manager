package com.book.manager.infrastructure.database.mapper

import com.book.manager.infrastructure.database.mapper.BookDynamicSqlSupport.author
import com.book.manager.infrastructure.database.mapper.BookDynamicSqlSupport.book
import com.book.manager.infrastructure.database.mapper.BookDynamicSqlSupport.id
import com.book.manager.infrastructure.database.mapper.BookDynamicSqlSupport.releaseDate
import com.book.manager.infrastructure.database.mapper.BookDynamicSqlSupport.title
import com.book.manager.infrastructure.database.mapper.RentalDynamicSqlSupport.accountId
import com.book.manager.infrastructure.database.mapper.RentalDynamicSqlSupport.rental
import com.book.manager.infrastructure.database.mapper.RentalDynamicSqlSupport.rentalDatetime
import com.book.manager.infrastructure.database.mapper.RentalDynamicSqlSupport.returnDeadline
import com.book.manager.infrastructure.database.record.BookWithRental
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Result
import org.apache.ibatis.annotations.ResultMap
import org.apache.ibatis.annotations.Results
import org.apache.ibatis.annotations.SelectProvider
import org.apache.ibatis.type.JdbcType
import org.mybatis.dynamic.sql.select.render.SelectStatementProvider
import org.mybatis.dynamic.sql.util.SqlProviderAdapter
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.select

@Mapper
interface BookWithRentalMapper {
    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    @Results(
        id = "BookWithRentalRecordResult", value = [
            Result(column = "id", property = "id", jdbcType = JdbcType.BIGINT, id = true),
            Result(column = "title", property = "title", jdbcType = JdbcType.VARCHAR),
            Result(column = "author", property = "author", jdbcType = JdbcType.VARCHAR),
            Result(column = "release_date", property = "releaseDate", jdbcType = JdbcType.DATE),
            Result(column = "account_id", property = "accountId", jdbcType = JdbcType.BIGINT),
            Result(column = "rental_datetime", property = "rentalDateTime", jdbcType = JdbcType.TIMESTAMP),
            Result(column = "return_deadline", property = "returnDeadline", jdbcType = JdbcType.TIMESTAMP)
        ]
    )
    fun selectMany(selectStatement: SelectStatementProvider): List<BookWithRental>

    @SelectProvider(type = SqlProviderAdapter::class, method = "select")
    @ResultMap("BookWithRentalRecordResult")
    fun selectOne(selectStatement: SelectStatementProvider): BookWithRental?
}

private val columnList = listOf(
    id,
    title,
    author,
    releaseDate,
    accountId,
    rentalDatetime,
    returnDeadline
)

fun BookWithRentalMapper.select(): List<BookWithRental> =
    select(columnList) {
        from(book, "b")
        leftJoin(rental, "r") {
            on(book.id) equalTo rental.bookId
        }
    }.let { selectMany(it) }

fun BookWithRentalMapper.selectByPrimaryKey(bookId: Long): BookWithRental? =
    select(columnList) {
        from(book, "b")
        leftJoin(rental, "r") {
            on(book.id) equalTo rental.bookId
        }
        where { book.id.isEqualTo(bookId) }
    }.let { selectOne(it) }
