package com.book.manager.application.service

import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import com.book.manager.domain.repository.BookRepository
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class AdminBookServiceTest {

    private val bookRepository = mock<BookRepository>()
    private val adminBookService = AdminBookService(bookRepository)

    private val book = Book(1L, "title", "hogehoge", LocalDate.now())

    @Test
    @DisplayName("書籍の登録処理")
    fun `register when book is not null then register the book`() {
        adminBookService.register(book)
        verify(bookRepository).register(book)
    }

    @Test
    @DisplayName("書籍が登録済みなら例外発生")
    fun `register when book has already been existed then throw Exception`() {
        whenever(bookRepository.findWithRental(any())).thenReturn(BookWithRental(book, null))

        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            adminBookService.register(book)
        }

        assertThat(exception.message).isEqualTo("既に存在する書籍ID: ${book.id}")
        verify(bookRepository, times(0)).register(any())
    }

    @Test
    @DisplayName("書籍の削除処理")
    fun `delete when book is no null then delete the book`() {
        whenever(bookRepository.findWithRental(any())).thenReturn(BookWithRental(book, null))

        adminBookService.delete(book.id)

        verify(bookRepository).delete(book.id)
    }

    @Test
    @DisplayName("書籍が存在しなければ削除で例外を発生する")
    fun `delete when book is not exist then throw Exception`() {
        whenever(bookRepository.findWithRental(any())).thenReturn(null)

        val exception = Assertions.assertThrows(IllegalArgumentException::class.java) {
            adminBookService.delete(book.id)
        }

        assertThat(exception.message).isEqualTo("存在しない書籍ID: ${book.id}")
        verify(bookRepository, times(0)).delete(any())
    }

    @Test
    @DisplayName("書籍の更新処理")
    fun `update when book is exists then update the book`() {
        whenever(bookRepository.findWithRental(book.id)).thenReturn(BookWithRental(book, null))

        adminBookService.update(book.id, "test", "author", LocalDate.now())

        verify(bookRepository).update(any(), any(), any(), any())
    }

    @Test
    @DisplayName("書籍が存在しなければ更新すると例外が発生する")
    fun `update when book is not exist then throw Exception`() {

        whenever(bookRepository.findWithRental(book.id)).thenReturn(null)

        val exception = Assertions.assertThrows(java.lang.IllegalArgumentException::class.java) {
            adminBookService.update(book.id, "test", "author", LocalDate.now())
        }

        assertThat(exception.message).isEqualTo("存在しない書籍ID: ${book.id}")
        verify(bookRepository, times(0)).update(any(), any(), any(), any())
    }
}