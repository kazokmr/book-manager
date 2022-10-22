/*
 * Auto-generated file. Created by MyBatis Generator
 */
package com.book.manager.infrastructure.database.mapper

import com.book.manager.infrastructure.database.mapper.BookDynamicSqlSupport.author
import com.book.manager.infrastructure.database.mapper.BookDynamicSqlSupport.book
import com.book.manager.infrastructure.database.mapper.BookDynamicSqlSupport.id
import com.book.manager.infrastructure.database.mapper.BookDynamicSqlSupport.releaseDate
import com.book.manager.infrastructure.database.mapper.BookDynamicSqlSupport.title
import com.book.manager.infrastructure.database.record.Book
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
interface BookMapper : CommonCountMapper, CommonDeleteMapper, CommonInsertMapper<Book>, CommonUpdateMapper {
    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @Results(id="BookResult", value = [
        Result(column="id", property="id", jdbcType=JdbcType.BIGINT, id=true),
        Result(column="title", property="title", jdbcType=JdbcType.VARCHAR),
        Result(column="author", property="author", jdbcType=JdbcType.VARCHAR),
        Result(column="release_date", property="releaseDate", jdbcType=JdbcType.DATE)
    ])
    fun selectMany(selectStatement: SelectStatementProvider): List<Book>

    @SelectProvider(type=SqlProviderAdapter::class, method="select")
    @ResultMap("BookResult")
    fun selectOne(selectStatement: SelectStatementProvider): Book?
}

fun BookMapper.count(completer: CountCompleter) =
    countFrom(this::count, book, completer)

fun BookMapper.delete(completer: DeleteCompleter) =
    deleteFrom(this::delete, book, completer)

fun BookMapper.deleteByPrimaryKey(id_: Long) =
    delete {
        where { id isEqualTo id_ }
    }

fun BookMapper.insert(row: Book) =
    insert(this::insert, row, book) {
        map(id) toProperty "id"
        map(title) toProperty "title"
        map(author) toProperty "author"
        map(releaseDate) toProperty "releaseDate"
    }

fun BookMapper.insertMultiple(records: Collection<Book>) =
    insertMultiple(this::insertMultiple, records, book) {
        map(id) toProperty "id"
        map(title) toProperty "title"
        map(author) toProperty "author"
        map(releaseDate) toProperty "releaseDate"
    }

fun BookMapper.insertMultiple(vararg records: Book) =
    insertMultiple(records.toList())

fun BookMapper.insertSelective(row: Book) =
    insert(this::insert, row, book) {
        map(id).toPropertyWhenPresent("id", row::id)
        map(title).toPropertyWhenPresent("title", row::title)
        map(author).toPropertyWhenPresent("author", row::author)
        map(releaseDate).toPropertyWhenPresent("releaseDate", row::releaseDate)
    }

private val columnList = listOf(id, title, author, releaseDate)

fun BookMapper.selectOne(completer: SelectCompleter) =
    selectOne(this::selectOne, columnList, book, completer)

fun BookMapper.select(completer: SelectCompleter) =
    selectList(this::selectMany, columnList, book, completer)

fun BookMapper.selectDistinct(completer: SelectCompleter) =
    selectDistinct(this::selectMany, columnList, book, completer)

fun BookMapper.selectByPrimaryKey(id_: Long) =
    selectOne {
        where { id isEqualTo id_ }
    }

fun BookMapper.update(completer: UpdateCompleter) =
    update(this::update, book, completer)

fun KotlinUpdateBuilder.updateAllColumns(row: Book) =
    apply {
        set(id) equalToOrNull row::id
        set(title) equalToOrNull row::title
        set(author) equalToOrNull row::author
        set(releaseDate) equalToOrNull row::releaseDate
    }

fun KotlinUpdateBuilder.updateSelectiveColumns(row: Book) =
    apply {
        set(id) equalToWhenPresent row::id
        set(title) equalToWhenPresent row::title
        set(author) equalToWhenPresent row::author
        set(releaseDate) equalToWhenPresent row::releaseDate
    }

fun BookMapper.updateByPrimaryKey(row: Book) =
    update {
        set(title) equalToOrNull row::title
        set(author) equalToOrNull row::author
        set(releaseDate) equalToOrNull row::releaseDate
        where { id isEqualTo row.id!! }
    }

fun BookMapper.updateByPrimaryKeySelective(row: Book) =
    update {
        set(title) equalToWhenPresent row::title
        set(author) equalToWhenPresent row::author
        set(releaseDate) equalToWhenPresent row::releaseDate
        where { id isEqualTo row.id!! }
    }