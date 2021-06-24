package com.book.manager.application.service

import com.book.manager.application.service.result.Result
import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import com.book.manager.domain.repository.BookRepository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.format.DateTimeFormatter

internal class BookServiceTest {

    private lateinit var bookRepository: BookRepository
    private lateinit var bookService: BookService

    @BeforeEach
    internal fun setUp() {
        bookRepository = mock()
        bookService = BookService(bookRepository)
    }

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
        whenever(bookRepository.findAllWithRental()).thenReturn(expected)

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
        whenever(bookRepository.findAllWithRental()).thenReturn(expected)

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
        whenever(bookRepository.findWithRental(any() as Long)).thenReturn(bookWithRental)

        // When
        val result = bookService.getDetail(book.id)

        // Then
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data).isEqualTo(bookWithRental)
    }

    @Test
    @DisplayName("IDに該当する書籍がリストに無ければ検索失敗")
    fun `getDetail when book is not exist then return failure`() {

        // Given
        val bookId = 20L
        whenever(bookRepository.findWithRental(any() as Long)).thenReturn(null)

        // When
        val result = bookService.getDetail(bookId)

        // Then
        assertThat(result).isInstanceOf(Result.Failure::class.java)
        result as Result.Failure
        assertThat(result.message).isEqualTo("存在しない書籍ID: $bookId")
    }
}