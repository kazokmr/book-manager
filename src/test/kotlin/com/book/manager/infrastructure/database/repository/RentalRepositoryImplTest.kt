package com.book.manager.infrastructure.database.repository

import com.book.manager.domain.model.Rental
import com.book.manager.domain.repository.RentalRepository
import com.book.manager.infrastructure.database.dbunit.CsvDataSetLoader
import com.book.manager.infrastructure.database.testcontainers.TestContainerPostgres
import com.github.springtestdbunit.DbUnitTestExecutionListener
import com.github.springtestdbunit.annotation.DatabaseSetup
import com.github.springtestdbunit.annotation.DbUnitConfiguration
import com.github.springtestdbunit.annotation.ExpectedDatabase
import com.github.springtestdbunit.assertion.DatabaseAssertionMode.NON_STRICT_UNORDERED
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.context.annotation.Import
import org.springframework.dao.DuplicateKeyException
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import java.time.LocalDateTime

@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(RentalRepositoryImpl::class)
@DbUnitConfiguration(dataSetLoader = CsvDataSetLoader::class)
@TestExecutionListeners(listeners = [DependencyInjectionTestExecutionListener::class, DbUnitTestExecutionListener::class])
internal class RentalRepositoryImplTest : TestContainerPostgres() {

    @Autowired
    private lateinit var rentalRepository: RentalRepository

    @Test
    @DisplayName("Rentalテーブルへの登録")
    @DatabaseSetup("/testdata/rental/init-data")
    @ExpectedDatabase("/testdata/rental/after-start-rental", assertionMode = NON_STRICT_UNORDERED)
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

        // Then
        assertThat(registeredCount).isEqualTo(1)
    }

    @Test
    @DisplayName("登録済みの書籍IDがあれば、Rentalテーブルに登録しない")
    @DatabaseSetup("/testdata/rental/init-data")
    @ExpectedDatabase("/testdata/rental/after-no-update", assertionMode = NON_STRICT_UNORDERED)
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
    @DatabaseSetup("/testdata/rental/init-data")
    @ExpectedDatabase("/testdata/rental/after-end-rental", assertionMode = NON_STRICT_UNORDERED)
    fun `endRental when rental is exist then delete`() {

        // Given
        val bookId = 999L

        // When
        val deleteCount = rentalRepository.endRental(bookId)

        // Then
        assertThat(deleteCount).isEqualTo(1)
    }

    @Test
    @DisplayName("Rental情報が登録されていなければ削除しない")
    @DatabaseSetup("/testdata/rental/init-data")
    @ExpectedDatabase("/testdata/rental/after-no-update", assertionMode = NON_STRICT_UNORDERED)
    fun `endRental when recode is not exist then does not delete`() {

        // Given
        val bookId = 500L

        // When
        val count = rentalRepository.endRental(bookId)

        // Then
        assertThat(count).isEqualTo(0)
    }
}