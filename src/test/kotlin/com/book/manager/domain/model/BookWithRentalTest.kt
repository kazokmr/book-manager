package com.book.manager.domain.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class BookWithRentalTest {

    @Test
    @DisplayName("RentalオブジェクトがNullならFalseを返す")
    fun `isRental when rental is null then return false`() {
        val book = Book(1, "Kotlin入門", "コトリン太郎", LocalDate.now())
        val bookWithRental = BookWithRental(book, null)
        assertThat(bookWithRental.isRental).`as`("When Rental is Null then return false").isFalse
    }

    @Test
    @DisplayName("RentalオブジェクトがNotNullならTrueを返す")
    fun `isRental when rental is not null then return true`() {
        val book = Book(1, "Kotlin入門", "コトリン太郎", LocalDate.now())
        val rental = Rental(1, 100, LocalDateTime.now(), LocalDateTime.now().plusDays(14))
        val bookWithRental = BookWithRental(book, rental)
        assertThat(bookWithRental.isRental).`as`("When Rental is not Null then return true").isTrue
    }
}