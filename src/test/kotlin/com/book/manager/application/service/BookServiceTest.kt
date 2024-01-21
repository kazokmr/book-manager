package com.book.manager.application.service

import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import com.book.manager.domain.repository.BookRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@ExtendWith(MockitoExtension::class)
internal class BookServiceTest {

    @InjectMocks
    private lateinit var bookService: BookService

    @Mock
    private lateinit var bookRepository: BookRepository

    @Test
    @DisplayName("書籍リストがあればリストを返す")
    fun `getList when book list is exist then return list`() {

        // Given
        val bookA = Book(1, "Kotlin入門", "コトリン太郎", LocalDate.now())
        val bookB =
            Book(10, "Java入門", "ジャバ花子", LocalDate.parse("2018/10/01", DateTimeFormatter.ofPattern("yyyy/MM/dd")))
        val bookWithRentalA = BookWithRental(bookA, null)
        val bookWithRentalB = BookWithRental(bookB, null)

        val expected = listOf(bookWithRentalA, bookWithRentalB)
        doReturn(expected).`when`(bookRepository).findAllWithRental()

        // When
        val result = bookService.getList()

        // Then
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("書籍リストが無ければ空のリストを返す")
    fun `getList when book list is not exist then return empty list`() {

        // Given
        val expected = emptyList<BookWithRental>()
        doReturn(expected).`when`(bookRepository).findAllWithRental()

        // When
        val result = bookService.getList()

        // Then
        assertThat(result).isEmpty()
    }

    @Test
    @DisplayName("IDに該当する書籍をリストから検索する")
    fun `getDetail when book is exist then return bookWithRental`() {

        // Given
        val book = Book(528, "Kotlin入門", "Kotlin太郎", LocalDate.now())
        val bookWithRental = BookWithRental(book, null)
        doReturn(bookWithRental).`when`(bookRepository).findWithRental(any())

        // When
        val result = bookService.getDetail(book.id)

        // Then
        assertThat(result).isEqualTo(bookWithRental)
    }

    @Test
    @DisplayName("IDに該当する書籍がリストに無ければ検索失敗")
    fun `getDetail when book is not exist then return failure`() {

        // Given
        val bookId = 20L
        doReturn(null).`when`(bookRepository).findWithRental(any())

        // When
        val result = assertThrows<IllegalArgumentException> { bookService.getDetail(bookId) }

        // Then
        assertThat(result.message).isEqualTo("存在しない書籍ID: $bookId")
    }
}
