package com.book.manager.infrastructure.database.mapper

import com.book.manager.infrastructure.database.mapper.BookDynamicSqlSupport.Book
import com.book.manager.infrastructure.database.mapper.BookDynamicSqlSupport.Book.author
import com.book.manager.infrastructure.database.mapper.BookDynamicSqlSupport.Book.id
import com.book.manager.infrastructure.database.mapper.BookDynamicSqlSupport.Book.releaseDate
import com.book.manager.infrastructure.database.mapper.BookDynamicSqlSupport.Book.title
import com.book.manager.infrastructure.database.mapper.RentalDynamicSqlSupport.Rental
import com.book.manager.infrastructure.database.mapper.RentalDynamicSqlSupport.Rental.accountId
import com.book.manager.infrastructure.database.mapper.RentalDynamicSqlSupport.Rental.rentalDatetime
import com.book.manager.infrastructure.database.mapper.RentalDynamicSqlSupport.Rental.returnDeadline
import com.book.manager.infrastructure.database.record.BookWithRentalRecord
import org.mybatis.dynamic.sql.util.kotlin.mybatis3.select

private val columnList = listOf(
    id,
    title,
    author,
    releaseDate,
    accountId,
    rentalDatetime,
    returnDeadline
)

fun BookWithRentalMapper.select(): List<BookWithRentalRecord> =
    select(columnList) {
        from(Book, "b")
        leftJoin(Rental, "r") {
            on(id) equalTo Rental.bookId
        }
    }.let { selectMany(it) }

fun BookWithRentalMapper.selectByPrimaryKey(bookId: Long): BookWithRentalRecord? =
    select(columnList) {
        from(Book, "b")
        leftJoin(Rental, "r") {
            on(id) equalTo Rental.bookId
        }
        where { id.isEqualTo(bookId) }
    }.let { selectOne(it) }