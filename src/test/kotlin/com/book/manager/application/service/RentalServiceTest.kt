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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
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
    fun `startRental when book is not rental then start to rental`() {

        // Given
        whenever(accountRepository.findById(any() as Long)).thenReturn(account)
        whenever(bookRepository.findWithRental(any() as Long)).thenReturn(BookWithRental(book, null))

        // When
        rentalService.startRental(book.id, account.id)

        // Then
        verify(rentalRepository).startRental(any() as Rental)
    }

    @Test
    @DisplayName("該当ユーザーが存在しなければ書籍は貸出しできない")
    fun `startRental when account is not exist then throws Exception`() {

        // Given
        whenever(accountRepository.findById(any() as Long)).thenReturn(null)

        // When
        val exception = assertThrows(IllegalArgumentException::class.java) {
            rentalService.startRental(book.id, account.id)
        }

        // Then
        assertThat(exception.message).`as`("エラーメッセージ").isEqualTo("該当するユーザーが存在しません accountId: ${account.id}")

        verify(accountRepository).findById(account.id)
        verify(bookRepository, times(0)).findWithRental(any() as Long)
        verify(rentalRepository, times(0)).startRental(any() as Rental)
    }

    @Test
    @DisplayName("該当する書籍が存在しなければ貸出しできない")
    fun `startRental when book is not exist then throws Exception`() {

        // Given
        whenever(accountRepository.findById(any() as Long)).thenReturn(account)
        whenever(bookRepository.findWithRental(any() as Long)).thenReturn(null)

        // When
        val exception = assertThrows(IllegalArgumentException::class.java) {
            rentalService.startRental(book.id, account.id)
        }

        // Then
        assertThat(exception.message).`as`("エラーメッセージ").isEqualTo("該当する書籍が存在しません bookId: ${book.id}")

        verify(accountRepository).findById(account.id)
        verify(bookRepository).findWithRental(book.id)
        verify(rentalRepository, times(0)).startRental(any() as Rental)
    }

    @Test
    @DisplayName("書籍が貸出中なら貸出しできない")
    fun `startRental when book has been renting then throw Exception`() {

        //Give
        whenever(accountRepository.findById(any() as Long)).thenReturn(account)
        val rental = Rental(book.id, 999, LocalDateTime.now(), LocalDateTime.now().plusDays(14))
        val rentBook = BookWithRental(book, rental)
        whenever(bookRepository.findWithRental(book.id)).thenReturn(rentBook)

        // When
        val exception = assertThrows(IllegalArgumentException::class.java) {
            rentalService.startRental(book.id, account.id)
        }

        // Then
        assertThat(exception.message).`as`("エラーメッセージ").isEqualTo("貸出中の書籍です bookId: ${book.id}")

        verify(accountRepository).findById(account.id)
        verify(bookRepository).findWithRental(book.id)
        verify(rentalRepository, times(0)).startRental(any() as Rental)
    }

    @Test
    @DisplayName("書籍が借りられていたら、貸出情報を削除する")
    fun `endRental when book is rental then delete to rental`() {

        // Given
        val rental = Rental(book.id, account.id, LocalDateTime.now(), LocalDateTime.now().plusDays(14))
        val bookWithRental = BookWithRental(book, rental)
        whenever(accountRepository.findById(any() as Long)).thenReturn(account)
        whenever(bookRepository.findWithRental(any() as Long)).thenReturn(bookWithRental)

        // When
        rentalService.endRental(book.id, account.id)

        // Then
        verify(accountRepository).findById(account.id)
        verify(bookRepository).findWithRental(book.id)
        verify(rentalRepository).endRental(book.id)
    }

    @Test
    @DisplayName("該当ユーザーが存在しなければ書籍は返却できない")
    fun `endRental when account is not exist then throws Exception`() {

        // Given
        whenever(accountRepository.findById(any() as Long)).thenReturn(null)

        // When
        val exception = assertThrows(IllegalArgumentException::class.java) {
            rentalService.endRental(book.id, account.id)
        }

        // Then
        assertThat(exception.message).`as`("エラーメッセージ").isEqualTo("該当するユーザーが存在しません accountId: ${account.id}")

        verify(accountRepository).findById(account.id)
        verify(bookRepository, times(0)).findWithRental(any() as Long)
        verify(rentalRepository, times(0)).endRental(any() as Long)
    }

    @Test
    @DisplayName("該当する書籍が存在しなければ返却できない")
    fun `endRental when book is not exist then throws Exception`() {

        // Given
        whenever(accountRepository.findById(any() as Long)).thenReturn(account)
        whenever(bookRepository.findWithRental(any() as Long)).thenReturn(null)

        // When
        val exception = assertThrows(IllegalArgumentException::class.java) {
            rentalService.endRental(book.id, account.id)
        }

        // Then
        assertThat(exception.message).`as`("エラーメッセージ").isEqualTo("該当する書籍が存在しません bookId: ${book.id}")

        verify(accountRepository).findById(account.id)
        verify(bookRepository).findWithRental(book.id)
        verify(rentalRepository, times(0)).endRental(any() as Long)
    }

    @Test
    @DisplayName("書籍が借りられていなかったら、返却できない")
    fun `endRental when book is not rental then throw exception`() {

        // Given
        whenever(accountRepository.findById(any() as Long)).thenReturn(account)
        whenever(bookRepository.findWithRental(any() as Long)).thenReturn(BookWithRental(book, null))

        // When
        val exception = assertThrows(IllegalArgumentException::class.java) {
            rentalService.endRental(book.id, account.id)
        }

        // Then
        assertThat(exception.message).`as`("エラーメッセージ").isEqualTo("未貸出の書籍です bookId: ${book.id}")

        verify(accountRepository).findById(account.id)
        verify(bookRepository).findWithRental(book.id)
        verify(rentalRepository, times(0)).endRental(any())
    }

    @Test
    @DisplayName("別のユーザーが借りている書籍なら、返却できない")
    fun `endRental when book has been renting by other account then throw exception`() {

        // Given
        whenever(accountRepository.findById(any() as Long)).thenReturn(account)
        val rental = Rental(book.id, 528, LocalDateTime.now(), LocalDateTime.now().plusDays(14))
        val rentBook = BookWithRental(book, rental)
        whenever(bookRepository.findWithRental(any() as Long)).thenReturn(rentBook)

        // When
        val exception = assertThrows(IllegalArgumentException::class.java) {
            rentalService.endRental(book.id, account.id)
        }

        // Then
        assertThat(exception.message).`as`("エラーメッセージ").isEqualTo("他のユーザーが貸出中の書籍です bookId: ${book.id}")

        verify(accountRepository).findById(account.id)
        verify(bookRepository).findWithRental(book.id)
        verify(rentalRepository, times(0)).endRental(any())
    }
}