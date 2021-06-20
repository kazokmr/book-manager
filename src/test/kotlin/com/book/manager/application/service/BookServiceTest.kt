package com.book.manager.application.service

import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import com.book.manager.domain.repository.BookRepository
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

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
        val book = Book(1, "Kotlin入門", "コトリン太郎", LocalDate.now())
        val bookWithRental = BookWithRental(book, null)
        val expected = listOf(bookWithRental)

        whenever(bookRepository.findAllWithRental()).thenReturn(expected)

        val result = bookService.getList()
        Assertions.assertThat(result).`as`("検索した書籍リストが一致する").isEqualTo(expected)
    }
}