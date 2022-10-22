/*
 * Auto-generated file. Created by MyBatis Generator
 */
package com.book.manager.infrastructure.database.mapper

import java.sql.JDBCType
import java.time.LocalDate
import org.mybatis.dynamic.sql.AliasableSqlTable
import org.mybatis.dynamic.sql.util.kotlin.elements.column

object BookDynamicSqlSupport {
    val book = Book()

    val id = book.id

    val title = book.title

    val author = book.author

    val releaseDate = book.releaseDate

    class Book : AliasableSqlTable<Book>("public.book", ::Book) {
        val id = column<Long>(name = "id", jdbcType = JDBCType.BIGINT)

        val title = column<String>(name = "title", jdbcType = JDBCType.VARCHAR)

        val author = column<String>(name = "author", jdbcType = JDBCType.VARCHAR)

        val releaseDate = column<LocalDate>(name = "release_date", jdbcType = JDBCType.DATE)
    }
}