package com.book.manager.application.service

import com.book.manager.application.service.result.Result
import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import com.book.manager.domain.repository.BookRepository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class AdminBookServiceTest {

    private lateinit var bookRepository: BookRepository
    private lateinit var adminBookService: AdminBookService
    private lateinit var book: Book

    @BeforeEach
    internal fun setUp() {
        bookRepository = mock()
        adminBookService = AdminBookService(bookRepository)
        book = Book(100, "title", "hogehoge", LocalDate.now())
    }

    @Test
    @DisplayName("書籍の登録")
    fun `register when book is not null then register the book`() {

        // When
        val result = adminBookService.register(book)

        // Then
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data).isEqualTo(book)
        verify(bookRepository).register(book)
    }

    @Test
    @DisplayName("書籍が既に存在していたら登録失敗")
    fun `register when book has already been existed then throw Exception`() {

        // Given
        whenever(bookRepository.findWithRental(any() as Long)).thenReturn(BookWithRental(book, null))

        // When
        val result = adminBookService.register(book)

        // Then
        assertThat(result).isInstanceOf(Result.Failure::class.java)
        result as Result.Failure
        assertThat(result.message).isEqualTo("既に存在する書籍ID: ${book.id}")
        verify(bookRepository, times(0)).register(any() as Book)
    }

    @Test
    @DisplayName("書籍の削除")
    fun `delete when book is no null then delete the book`() {

        // Given
        whenever(bookRepository.findWithRental(any() as Long)).thenReturn(BookWithRental(book, null))

        // When
        val result = adminBookService.delete(book.id)

        // Then
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data).isEqualTo(book.id)
        verify(bookRepository).delete(book.id)
    }

    @Test
    @DisplayName("書籍が存在しなければ削除失敗")
    fun `delete when book is not exist then throw Exception`() {

        // Given
        whenever(bookRepository.findWithRental(any() as Long)).thenReturn(null)

        // When
        val result = adminBookService.delete(book.id)

        // Then
        assertThat(result).isInstanceOf(Result.Failure::class.java)
        result as Result.Failure
        assertThat(result.message).isEqualTo("存在しない書籍ID: ${book.id}")
        verify(bookRepository, times(0)).delete(any() as Long)
    }

    @Test
    @DisplayName("書籍の更新")
    fun `update when book is exists then update the book`() {

        // Given
        whenever(bookRepository.findWithRental(any() as Long)).thenReturn(BookWithRental(book, null))

        // When
        val result = adminBookService.update(book.id, "test", "author", LocalDate.now())

        // Then
        assertThat(result).isInstanceOf(Result.Success::class.java)
        result as Result.Success
        assertThat(result.data).isEqualTo(book.id)
        verify(bookRepository).update(any() as Long, any() as String, any() as String, any() as LocalDate)
    }

    @Test
    @DisplayName("書籍が存在しなければ更新失敗")
    fun `update when book is not exist then throw Exception`() {

        // Given
        whenever(bookRepository.findWithRental(any() as Long)).thenReturn(null)

        // When
        val result = adminBookService.update(book.id, "test", "author", LocalDate.now())

        // Then
        assertThat(result).isInstanceOf(Result.Failure::class.java)
        result as Result.Failure
        assertThat(result.message).isEqualTo("存在しない書籍ID: ${book.id}")
        verify(bookRepository, times(0)).update(any() as Long, any() as String, any() as String, any() as LocalDate)
    }
}