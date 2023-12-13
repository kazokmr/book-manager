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
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
internal class RentalServiceTest {

    @InjectMocks
    private lateinit var rentalService: RentalService

    @Mock
    private lateinit var accountRepository: AccountRepository

    @Mock
    private lateinit var bookRepository: BookRepository

    @Mock
    private lateinit var rentalRepository: RentalRepository

    private lateinit var account: Account
    private lateinit var book: Book

    @BeforeEach
    internal fun setUp() {
        account = Account(100, "test@example.com", "pass", "kotlin", RoleType.USER)
        book = Book(100, "Kotlin入門", "コトリン太郎", LocalDate.now())
    }

    @Test
    @DisplayName("書籍の貸出登録を行う")
    fun `startRental when book is not rental then return success`() {

        // Given
        doReturn(account).`when`(accountRepository).findById(any())
        doReturn(BookWithRental(book, null)).`when`(bookRepository).findWithRental(any())

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
        doReturn(null).`when`(accountRepository).findById(any())

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
        doReturn(account).`when`(accountRepository).findById(any())
        doReturn(null).`when`(bookRepository).findWithRental(any())
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
        doReturn(account).`when`(accountRepository).findById(any())
        val rental = Rental(book.id, 999, LocalDateTime.now(), LocalDateTime.now().plusDays(14))
        val rentBook = BookWithRental(book, rental)
        doReturn(rentBook).`when`(bookRepository).findWithRental(book.id)

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
        doReturn(account).`when`(accountRepository).findById(any())
        doReturn(bookWithRental).`when`(bookRepository).findWithRental(any())

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
        doReturn(null).`when`(accountRepository).findById(any())

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
        doReturn(account).`when`(accountRepository).findById(any())
        doReturn(null).`when`(bookRepository).findWithRental(any())

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
        doReturn(account).`when`(accountRepository).findById(any())
        doReturn(BookWithRental(book, null)).`when`(bookRepository).findWithRental(any())

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
        doReturn(account).`when`(accountRepository).findById(any())
        val rental = Rental(book.id, 528, LocalDateTime.now(), LocalDateTime.now().plusDays(14))
        val rentBook = BookWithRental(book, rental)
        doReturn(rentBook).`when`(bookRepository).findWithRental(any())

        // When
        val result = assertThrows<IllegalArgumentException> { rentalService.endRental(book.id, account.id) }

        // Then
        verify(accountRepository).findById(account.id)
        verify(bookRepository).findWithRental(book.id)
        verify(rentalRepository, times(0)).endRental(any())
        assertThat(result.message).isEqualTo("他のユーザーが貸出中の書籍です bookId: ${book.id}")
    }
}
