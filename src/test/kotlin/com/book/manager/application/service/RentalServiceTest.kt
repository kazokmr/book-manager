package com.book.manager.application.service

import com.book.manager.domain.enum.RoleType
import com.book.manager.domain.model.Account
import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import com.book.manager.domain.model.Rental
import com.book.manager.domain.repository.AccountRepository
import com.book.manager.domain.repository.BookRepository
import com.book.manager.domain.repository.RentalRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.time.LocalDateTime

internal class RentalServiceTest {

    private lateinit var accountRepository: AccountRepository
    private lateinit var bookRepository: BookRepository
    private lateinit var rentalRepository: RentalRepository

    private lateinit var rentalService: RentalService

    private lateinit var account: Account
    private lateinit var book: Book

    @BeforeEach
    internal fun setUp() {
        accountRepository = mock()
        bookRepository = mock()
        rentalRepository = mock()
        rentalService = RentalService(accountRepository, bookRepository, rentalRepository)

        account = Account(100, "test@example.com", "pass", "kotlin", RoleType.USER)
        book = Book(100, "Kotlin入門", "コトリン太郎", LocalDate.now())
    }

    @Test
    @DisplayName("書籍の貸出登録を行う")
    fun `startRental when book is not rental then return success`() {

        // Given
        whenever(accountRepository.findById(any())).thenReturn(account)
        whenever(bookRepository.findWithRental(any())).thenReturn(BookWithRental(book, null))

        // When
        val result = rentalService.startRental(book.id, account.id)

        // Then
        verify(rentalRepository).startRental(any())
        SoftAssertions().apply {
            assertThat(result.bookId).isEqualTo(book.id)
            assertThat(result.accountId).isEqualTo(account.id)
            assertThat(result.rentalDatetime).isNotNull
            assertThat(result.returnDeadline).isEqualTo(result.rentalDatetime.plusDays(14))
        }.assertAll()
    }

    @Test
    @DisplayName("該当ユーザーが存在しなければ書籍は貸出しできない")
    fun `startRental when account is not exist then return failure`() {

        // Given
        whenever(accountRepository.findById(any())).thenReturn(null)

        // When
        val result = assertThrows<IllegalArgumentException> { rentalService.startRental(book.id, account.id) }

        // Then
        verify(accountRepository).findById(account.id)
        verify(bookRepository, times(0)).findWithRental(any())
        verify(rentalRepository, times(0)).startRental(any())
        assertThat(result.message).isEqualTo("該当するユーザーが存在しません accountId: ${account.id}")
    }

    @Test
    @DisplayName("該当する書籍が存在しなければ貸出しできない")
    fun `startRental when book is not exist then return failure`() {

        // Given
        whenever(accountRepository.findById(any())).thenReturn(account)
        whenever(bookRepository.findWithRental(any())).thenReturn(null)

        // When
        val result = assertThrows<IllegalArgumentException> { rentalService.startRental(book.id, account.id) }

        // Then
        verify(accountRepository).findById(account.id)
        verify(bookRepository).findWithRental(book.id)
        verify(rentalRepository, times(0)).startRental(any())
        assertThat(result.message).isEqualTo("該当する書籍が存在しません bookId: ${book.id}")
    }

    @Test
    @DisplayName("書籍が貸出中なら貸出しできない")
    fun `startRental when book has been renting then return failure`() {

        //Give
        whenever(accountRepository.findById(any())).thenReturn(account)
        val rental = Rental(book.id, 999, LocalDateTime.now(), LocalDateTime.now().plusDays(14))
        val rentBook = BookWithRental(book, rental)
        whenever(bookRepository.findWithRental(book.id)).thenReturn(rentBook)

        // When
        val result = assertThrows<IllegalArgumentException> { rentalService.startRental(book.id, account.id) }

        // Then
        verify(accountRepository).findById(account.id)
        verify(bookRepository).findWithRental(book.id)
        verify(rentalRepository, times(0)).startRental(any())
        assertThat(result.message).isEqualTo("貸出中の書籍です bookId: ${book.id}")
    }

    @Test
    @DisplayName("書籍が借りられていたら、貸出情報を削除する")
    fun `endRental when book is rental then success delete`() {

        // Given
        val rental = Rental(book.id, account.id, LocalDateTime.now(), LocalDateTime.now().plusDays(14))
        val bookWithRental = BookWithRental(book, rental)
        whenever(accountRepository.findById(any())).thenReturn(account)
        whenever(bookRepository.findWithRental(any())).thenReturn(bookWithRental)

        // When
        val result = rentalService.endRental(book.id, account.id)

        // Then
        verify(accountRepository).findById(account.id)
        verify(bookRepository).findWithRental(book.id)
        verify(rentalRepository).endRental(book.id)
        assertThat(result).isEqualTo(book.id)
    }

    @Test
    @DisplayName("該当ユーザーが存在しなければ書籍は返却できない")
    fun `endRental when account is not exist then return failure`() {

        // Given
        whenever(accountRepository.findById(any())).thenReturn(null)

        // When
        val result = assertThrows<IllegalArgumentException> { rentalService.endRental(book.id, account.id) }

        // Then
        verify(accountRepository).findById(account.id)
        verify(bookRepository, times(0)).findWithRental(any())
        verify(rentalRepository, times(0)).endRental(any())
        assertThat(result.message).isEqualTo("該当するユーザーが存在しません accountId: ${account.id}")
    }

    @Test
    @DisplayName("該当する書籍が存在しなければ返却できない")
    fun `endRental when book is not exist then return failure`() {

        // Given
        whenever(accountRepository.findById(any())).thenReturn(account)
        whenever(bookRepository.findWithRental(any())).thenReturn(null)

        // When
        val result = assertThrows<IllegalArgumentException> { rentalService.endRental(book.id, account.id) }

        // Then
        verify(accountRepository).findById(account.id)
        verify(bookRepository).findWithRental(book.id)
        verify(rentalRepository, times(0)).endRental(any())
        assertThat(result.message).isEqualTo("該当する書籍が存在しません bookId: ${book.id}")
    }

    @Test
    @DisplayName("書籍が借りられていなかったら、返却できない")
    fun `endRental when book is not rental then return failure`() {

        // Given
        whenever(accountRepository.findById(any())).thenReturn(account)
        whenever(bookRepository.findWithRental(any())).thenReturn(BookWithRental(book, null))

        // When
        val result = assertThrows<IllegalArgumentException> { rentalService.endRental(book.id, account.id) }

        // Then
        verify(accountRepository).findById(account.id)
        verify(bookRepository).findWithRental(book.id)
        verify(rentalRepository, times(0)).endRental(any())
        assertThat(result.message).isEqualTo("未貸出の書籍です bookId: ${book.id}")
    }

    @Test
    @DisplayName("別のユーザーが借りている書籍なら、返却できない")
    fun `endRental when book has been renting by other account then return failure`() {

        // Given
        whenever(accountRepository.findById(any())).thenReturn(account)
        val rental = Rental(book.id, 528, LocalDateTime.now(), LocalDateTime.now().plusDays(14))
        val rentBook = BookWithRental(book, rental)
        whenever(bookRepository.findWithRental(any())).thenReturn(rentBook)

        // When
        val result = assertThrows<IllegalArgumentException> { rentalService.endRental(book.id, account.id) }

        // Then
        verify(accountRepository).findById(account.id)
        verify(bookRepository).findWithRental(book.id)
        verify(rentalRepository, times(0)).endRental(any())
        assertThat(result.message).isEqualTo("他のユーザーが貸出中の書籍です bookId: ${book.id}")
    }
}