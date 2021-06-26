package com.book.manager.infrastructure.database.repository

import com.book.manager.domain.model.Rental
import com.book.manager.domain.repository.RentalRepository
import com.book.manager.infrastructure.database.mapper.RentalMapper
import com.book.manager.infrastructure.database.mapper.deleteByPrimaryKey
import com.book.manager.infrastructure.database.mapper.insert
import com.book.manager.infrastructure.database.mapper.selectByPrimaryKey
import com.book.manager.infrastructure.database.record.RentalRecord
import com.book.manager.infrastructure.database.testcontainers.TestContainerPostgres
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.context.annotation.Import
import org.springframework.dao.DuplicateKeyException
import java.time.LocalDateTime

@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(RentalRepositoryImpl::class)
internal class RentalRepositoryImplTest : TestContainerPostgres() {

    @Autowired
    private lateinit var rentalRepository: RentalRepository

    @Autowired
    private lateinit var rentalMapper: RentalMapper

    private lateinit var rentalRecord: RentalRecord

    @BeforeEach
    internal fun setUp() {
        rentalRecord = RentalRecord(100, 222, LocalDateTime.now(), LocalDateTime.now().plusDays(14))
    }

    @Test
    @DisplayName("Rentalテーブルへの登録")
    fun `startRental when star renting then register Rental table`() {

        //Given
        rentalMapper.deleteByPrimaryKey(rentalRecord.bookId!!)
        val rental = Rental(
            rentalRecord.bookId!!,
            rentalRecord.accountId!!,
            rentalRecord.rentalDatetime!!,
            rentalRecord.returnDeadline!!
        )

        // When
        val count = rentalRepository.startRental(rental)

        // Then
        assertThat(count).isEqualTo(1)
        val result = rentalMapper.selectByPrimaryKey(rentalRecord.bookId!!)
        assertThat(result).isNotNull
        SoftAssertions().apply {
            assertThat(result?.bookId).isEqualTo(rentalRecord.bookId)
            assertThat(result?.accountId).isEqualTo(rentalRecord.accountId)
            assertThat(result?.rentalDatetime).isEqualTo(rentalRecord.rentalDatetime)
            assertThat(result?.returnDeadline).isEqualTo(rentalRecord.returnDeadline)
        }.assertAll()
    }

    @Test
    @DisplayName("登録済みの書籍IDがあれば、Rentalテーブルに登録しない")
    fun `startRental when bookId has already been rent then is not registering Rental Table`() {

        // Given
        rentalMapper.deleteByPrimaryKey(rentalRecord.bookId!!)
        val rentItem =
            Rental(rentalRecord.bookId!!, 333, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(13))
        rentalRepository.startRental(rentItem)

        // When
        val rental = Rental(
            rentalRecord.bookId!!,
            rentalRecord.accountId!!,
            rentalRecord.rentalDatetime!!,
            rentalRecord.returnDeadline!!
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
        rentalMapper.deleteByPrimaryKey(rentalRecord.bookId!!)
        rentalMapper.insert(rentalRecord)


        // When
        val count = rentalRepository.endRental(rentalRecord.bookId!!)

        // Then
        assertThat(count).isEqualTo(1)
        rentalMapper.selectByPrimaryKey(rentalRecord.bookId!!).let { assertThat(it).isNull() }
    }

    @Test
    @DisplayName("Rental情報が登録されていなければ削除しない")
    fun `endRental when recode is not exist then does not delete`() {

        // Given
        rentalMapper.deleteByPrimaryKey(rentalRecord.bookId!!)

        // When
        val count = rentalRepository.endRental(rentalRecord.bookId!!)

        // Then
        assertThat(count).isEqualTo(0)
        rentalMapper.selectByPrimaryKey(rentalRecord.bookId!!).let { assertThat(it).isNull() }
    }
}