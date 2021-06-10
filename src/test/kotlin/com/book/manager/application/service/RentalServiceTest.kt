package com.book.manager.application.service

import com.book.manager.domain.enum.RoleType
import com.book.manager.domain.model.Account
import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import com.book.manager.domain.model.Rental
import com.book.manager.domain.repository.AccountRepository
import com.book.manager.domain.repository.BookRepository
import com.book.manager.domain.repository.RentalRepository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class RentalServiceTest {

    private val accountRepository = mock<AccountRepository>()
    private val bookRepository = mock<BookRepository>()
    private val rentalRepository = mock<RentalRepository>()

    private val rentalService = RentalService(accountRepository, bookRepository, rentalRepository)

    @Test
    @DisplayName("書籍が借りられていたら、貸出情報を削除する")
    fun `endRental when book is rental then delete to rental`() {
        val accountId = 100L
        val bookId = 1000L
        val account = Account(accountId, "test@test.com", "pass", "kotlin", RoleType.USER)
        val book = Book(bookId, "Kotlin入門", "コトリン太郎", LocalDate.now())
        val rental = Rental(bookId, accountId, LocalDateTime.now(), LocalDateTime.now().plusDays(14))
        val bookWithRental = BookWithRental(book, rental)

        whenever(accountRepository.findById(any() as Long)).thenReturn(account)
        whenever(bookRepository.findWithRental(any() as Long)).thenReturn(bookWithRental)

        rentalService.endRental(bookId, accountId)

        verify(accountRepository).findById(accountId)
        verify(bookRepository).findWithRental(bookId)
        verify(rentalRepository).endRental(bookId)
    }

    @Test
    @DisplayName("書籍が借りられていなかったら、例外をthrowする")
    fun `endRental when book is not rental then throw exception`() {
        val accountId = 100L
        val bookId = 1000L
        val account = Account(accountId, "test@test.com", "pass", "kotlin", RoleType.USER)
        val book = Book(bookId, "Kotlin入門", "コトリン太郎", LocalDate.now())
        val bookWithRental = BookWithRental(book, null)

        whenever(accountRepository.findById(any() as Long)).thenReturn(account)
        whenever(bookRepository.findWithRental(any() as Long)).thenReturn(bookWithRental)

        val exception = assertThrows(IllegalArgumentException::class.java) {
            rentalService.endRental(bookId, accountId)
        }

        assertThat(exception.message).isEqualTo("未貸出の書籍です bookId:$bookId")

        verify(accountRepository).findById(accountId)
        verify(bookRepository).findWithRental(bookId)
        verify(rentalRepository, times(0)).endRental(any())
    }
}