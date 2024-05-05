package com.book.manager.infrastructure.database.repository

import com.book.manager.domain.model.Rental
import com.book.manager.domain.repository.RentalRepository
import com.book.manager.infrastructure.database.mapper.RentalMapper
import com.book.manager.infrastructure.database.testcontainers.DbTestContainerConfiguration
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.boot.testcontainers.context.ImportTestcontainers
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.core.DataClassRowMapper
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.jdbc.Sql
import java.time.LocalDateTime

@MybatisTest
@ImportTestcontainers(DbTestContainerConfiguration::class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Sql("Rental.sql")
internal class RentalRepositoryImplTest(
    @Autowired private val rentalMapper: RentalMapper,
    @Autowired private val jdbcTemplate: JdbcTemplate
) {

    private lateinit var rentalRepository: RentalRepository

    @BeforeEach
    fun setUp() {
        rentalRepository = RentalRepositoryImpl(rentalMapper)
    }

    @Test
    @DisplayName("Rentalテーブルへの登録")
    fun `startRental when star renting then register Rental table`() {

        //Given
        val rental = Rental(
            100,
            222,
            LocalDateTime.parse("2021-06-29T00:00:00.000"),
            LocalDateTime.parse("2021-07-12T00:00:00.000")
        )

        // When
        val registeredCount = rentalRepository.startRental(rental)
        val resultList = jdbcTemplate.query(
            "SELECT book_id,account_id,rental_datetime,return_deadline FROM rental ORDER BY book_id ",
            DataClassRowMapper(Rental::class.java)
        )

        // Then
        SoftAssertions().apply {
            assertThat(registeredCount).isEqualTo(1)
            assertThat(resultList).hasSize(3)
            assertThat(resultList[0].bookId).isEqualTo(1)
            assertThat(resultList[0].accountId).isEqualTo(528)
            assertThat(resultList[0].rentalDatetime).isEqualTo(LocalDateTime.of(2021, 6, 1, 12, 0, 0, 0))
            assertThat(resultList[0].returnDeadline).isEqualTo(LocalDateTime.of(2021, 6, 14, 12, 0, 0, 0))
            assertThat(resultList[1].bookId).isEqualTo(100)
            assertThat(resultList[1].accountId).isEqualTo(222)
            assertThat(resultList[1].rentalDatetime).isEqualTo(LocalDateTime.of(2021, 6, 29, 0, 0, 0, 0))
            assertThat(resultList[1].returnDeadline).isEqualTo(LocalDateTime.of(2021, 7, 12, 0, 0, 0, 0))
            assertThat(resultList[2].bookId).isEqualTo(999)
            assertThat(resultList[2].accountId).isEqualTo(74)
            assertThat(resultList[2].rentalDatetime).isEqualTo(LocalDateTime.of(2021, 6, 29, 12, 0, 0, 0))
            assertThat(resultList[2].returnDeadline).isEqualTo(LocalDateTime.of(2021, 7, 12, 12, 0, 0, 0))
        }.assertAll()
    }

    @Test
    @DisplayName("登録済みの書籍IDがあれば、Rentalテーブルに登録しない")
    fun `startRental when bookId has already been rent then is not registering Rental Table`() {

        // Given
        val rental = Rental(
            1,
            222,
            LocalDateTime.parse("2021-06-29T00:00:00.000"),
            LocalDateTime.parse("2021-07-12T00:00:00.000")
        )

        // Then
        assertThrows<DuplicateKeyException> {
            rentalRepository.startRental(rental)
        }
    }

    @Test
    @DisplayName("Rentalテーブルからレコードを削除する")
    fun `endRental when rental is exist then delete`() {

        // Given
        val bookId = 999L

        // When
        val deleteCount = rentalRepository.endRental(bookId)
        val resultList = jdbcTemplate.query(
            "SELECT book_id,account_id,rental_datetime,return_deadline FROM rental ORDER BY book_id ",
            DataClassRowMapper(Rental::class.java)
        )

        // Then
        SoftAssertions().apply {
            assertThat(deleteCount).isEqualTo(1)
            assertThat(resultList).hasSize(1)
            assertThat(resultList[0].bookId).isEqualTo(1)
            assertThat(resultList[0].accountId).isEqualTo(528)
            assertThat(resultList[0].rentalDatetime).isEqualTo(LocalDateTime.of(2021, 6, 1, 12, 0, 0, 0))
            assertThat(resultList[0].returnDeadline).isEqualTo(LocalDateTime.of(2021, 6, 14, 12, 0, 0, 0))
        }.assertAll()
    }

    @Test
    @DisplayName("Rental情報が登録されていなければ削除しない")
    fun `endRental when recode is not exist then does not delete`() {

        // Given
        val bookId = 500L

        // When
        val count = rentalRepository.endRental(bookId)
        val resultList = jdbcTemplate.query(
            "SELECT book_id,account_id,rental_datetime,return_deadline FROM rental ORDER BY book_id",
            DataClassRowMapper(Rental::class.java)
        )

        // Then
        SoftAssertions().apply {
            assertThat(count).isEqualTo(0)
            assertThat(resultList).hasSize(2)
            assertThat(resultList[0].bookId).isEqualTo(1)
            assertThat(resultList[0].accountId).isEqualTo(528)
            assertThat(resultList[0].rentalDatetime).isEqualTo(LocalDateTime.of(2021, 6, 1, 12, 0, 0, 0))
            assertThat(resultList[0].returnDeadline).isEqualTo(LocalDateTime.of(2021, 6, 14, 12, 0, 0, 0))
            assertThat(resultList[1].bookId).isEqualTo(999)
            assertThat(resultList[1].accountId).isEqualTo(74)
            assertThat(resultList[1].rentalDatetime).isEqualTo(LocalDateTime.of(2021, 6, 29, 12, 0, 0, 0))
            assertThat(resultList[1].returnDeadline).isEqualTo(LocalDateTime.of(2021, 7, 12, 12, 0, 0, 0))
        }.assertAll()
    }
}
