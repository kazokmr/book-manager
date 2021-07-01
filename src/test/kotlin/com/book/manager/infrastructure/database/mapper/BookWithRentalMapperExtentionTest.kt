package com.book.manager.infrastructure.database.mapper

import com.book.manager.infrastructure.database.dbunit.DataSourceConfig
import com.book.manager.infrastructure.database.record.BookWithRentalRecord
import com.book.manager.infrastructure.database.testcontainers.TestContainerPostgres
import com.github.springtestdbunit.DbUnitTestExecutionListener
import com.github.springtestdbunit.annotation.DatabaseSetup
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import java.time.LocalDate
import java.time.LocalDateTime

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(value = [DataSourceConfig::class])
@TestExecutionListeners(listeners = [DependencyInjectionTestExecutionListener::class, DbUnitTestExecutionListener::class])
internal class BookWithRentalMapperExtensionsTest : TestContainerPostgres() {

    @Autowired
    private lateinit var mapper: BookWithRentalMapper

    @Test
    @DisplayName("書籍が0件なら空のリストを返す")
    @DatabaseSetup("/test-data/book-with-rental/init-data/no-book.xml")
    fun `select when there are no books then return empty list`() {

        // When
        val resultList = mapper.select()

        // Then
        assertThat(resultList).isEmpty()
    }

    @Test
    @DisplayName("書籍が1件で貸出中なら、貸出情報を含んだ書籍リストを1件返す")
    @DatabaseSetup("/test-data/book-with-rental/init-data/single-data-with-rental.xml")
    fun `select when there is only one book with renting then return list has a element`() {

        // Given
        val expectedRecord = BookWithRentalRecord(
            100,
            "Kotlin入門",
            "ことりん太郎",
            LocalDate.of(1950, 10, 1),
            3,
            LocalDateTime.of(2021, 7, 1, 5, 39, 48),
            LocalDateTime.of(2021, 7, 15, 5, 39, 48)
        )

        // When
        val resultList = mapper.select()

        // Then
        assertThat(resultList.count()).isEqualTo(1)
        val resultRecord = resultList[0]
        SoftAssertions().apply {
            assertThat(resultRecord.id).isEqualTo(expectedRecord.id)
            assertThat(resultRecord.title).isEqualTo(expectedRecord.title)
            assertThat(resultRecord.author).isEqualTo(expectedRecord.author)
            assertThat(resultRecord.releaseDate).isEqualTo(expectedRecord.releaseDate)
            assertThat(resultRecord.accountId).isEqualTo(expectedRecord.accountId)
            assertThat(resultRecord.rentalDateTime).isEqualTo(expectedRecord.rentalDateTime)
            assertThat(resultRecord.returnDeadline).isEqualTo(expectedRecord.returnDeadline)
        }.assertAll()
    }

    @Test
    @DisplayName("書籍が1件で貸出されていなければ、貸出情報を含まない書籍リストを1件返す")
    @DatabaseSetup("/test-data/book-with-rental/init-data/single-data-without-rental.xml")
    fun `select when there is only one book without renting then return list has a element`() {

        // Given
        val expectedRecord = BookWithRentalRecord(
            100,
            "Kotlin入門",
            "ことりん太郎",
            LocalDate.of(1950, 10, 1)
        )

        // When
        val resultList = mapper.select()

        // Then
        assertThat(resultList.count()).isEqualTo(1)
        val resultRecord = resultList[0]
        SoftAssertions().apply {
            assertThat(resultRecord.id).isEqualTo(expectedRecord.id)
            assertThat(resultRecord.title).isEqualTo(expectedRecord.title)
            assertThat(resultRecord.author).isEqualTo(expectedRecord.author)
            assertThat(resultRecord.accountId).isNull()
            assertThat(resultRecord.rentalDateTime).isNull()
            assertThat(resultRecord.returnDeadline).isNull()
        }.assertAll()
    }

    @Test
    @DisplayName("書籍が複数件ありその内の幾つかが貸出中のリストを返す")
    @DatabaseSetup("/test-data/book-with-rental/init-data/multi-data.xml")
    fun `select when there are some books with renting then return list`() {

        // Given
        val rentalDateTime = LocalDateTime.of(2021, 1, 24, 21, 1, 41)

        val expectedList = listOf<BookWithRentalRecord>(
            BookWithRentalRecord(
                100, "Kotlin入門", "ことりん太郎", LocalDate.of(1950, 10, 1)
            ),
            BookWithRentalRecord(
                200, "Java入門", "じゃば太郎", LocalDate.of(2005, 8, 29),
                2, rentalDateTime, rentalDateTime.plusDays(14)
            ),
            BookWithRentalRecord(
                300, "Spring入門", "すぷりんぐ太郎", LocalDate.of(2001, 3, 21),
                10, rentalDateTime, rentalDateTime.plusDays(14)
            ),
            BookWithRentalRecord(
                400, "Kotlin実践", "ことりん太郎", LocalDate.of(2020, 1, 25)
            )
        )

        // When
        val resultList = mapper.select()

        // Then
        assertThat(resultList).containsExactlyInAnyOrderElementsOf(expectedList)
    }

    @Test
    @DisplayName("IDに該当する書籍が無ければNullを返す")
    @DatabaseSetup("/test-data/book-with-rental/init-data/multi-data.xml")
    fun `selectByPrimaryKey when there is no book with specified id then return null`() {

        // Given
        val bookId = 999L

        // When
        val result = mapper.selectByPrimaryKey(bookId)

        // Then
        assertThat(result).isNull()
    }

    @Test
    @DisplayName("IDに該当する書籍があり貸出中なら、その書籍情報を返す")
    @DatabaseSetup("/test-data/book-with-rental/init-data/multi-data.xml")
    fun `selectByPrimaryKey when there is a book with specified id and it has been renting then return this`() {

        // Given
        val bookId = 200L
        val rentalDateTime = LocalDateTime.of(2021, 1, 24, 21, 1, 41)
        val expected = BookWithRentalRecord(
            bookId, "Java入門", "じゃば太郎", LocalDate.of(2005, 8, 29),
            2, rentalDateTime, rentalDateTime.plusDays(14)
        )

        // When
        val result = mapper.selectByPrimaryKey(bookId)

        // Then
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("IDに該当する書籍があり貸出されていなければ、その書籍情報を返す")
    @DatabaseSetup("/test-data/book-with-rental/init-data/multi-data.xml")
    fun `selectByPrimaryKey when there is a book with specified id and it has not been renting then return this`() {

        // Given
        val bookId = 400L
        val expected = BookWithRentalRecord(bookId, "Kotlin実践", "ことりん太郎", LocalDate.of(2020, 1, 25))

        // When
        val result = mapper.selectByPrimaryKey(bookId)

        // Then
        assertThat(result).isEqualTo(expected)
    }
}