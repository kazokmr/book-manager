package com.book.manager.presentation.controller

import com.book.manager.application.service.AdminBookService
import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import com.book.manager.domain.repository.BookRepository
import com.book.manager.presentation.form.RegisterBookRequest
import com.book.manager.presentation.form.UpdateBookRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDate

internal class AdminBookControllerTest {

    private lateinit var mockMvc: MockMvc

    private lateinit var bookRepository: BookRepository
    private lateinit var adminBookService: AdminBookService
    private lateinit var adminBookController: AdminBookController

    @BeforeEach
    internal fun setUp() {
        bookRepository = mock()
        adminBookService = AdminBookService(bookRepository)
        adminBookController = AdminBookController(adminBookService)
        mockMvc = MockMvcBuilders.standaloneSetup(adminBookController).build()
    }

    @Test
    @DisplayName("書籍を登録する")
    fun `register when book of request is not registered then the book is done`() {

        // Given
        val request = RegisterBookRequest(100L, "title", "author", LocalDate.now())
        val json = ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(request)
        val book = Book(request.id, request.title, request.author, request.releaseDate)

        whenever(bookRepository.findWithRental(any())).thenReturn(null)

        // When
        mockMvc.perform(post("/admin/book/register").contentType(APPLICATION_JSON).content(json))
            .andExpect(status().isOk)

        // Then
        verify(bookRepository).register(book)
    }

    @Test
    @DisplayName("書籍IDが登録済みなら登録しない")
    fun `register when book is exist then throw Exception`() {

        // Given
        val request = RegisterBookRequest(100L, "title", "author", LocalDate.now())
        val json = ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(request)
        val book = Book(request.id, request.title, request.author, request.releaseDate)

        whenever(bookRepository.findWithRental(any())).thenReturn(BookWithRental(book, null))

        // When
        val response = mockMvc.perform(post("/admin/book/register").contentType(APPLICATION_JSON).content(json))
            .andExpect(status().isBadRequest).andReturn().response

        // Then
        Assertions.assertThat(response.errorMessage).isEqualTo("既に存在する書籍ID: ${book.id}")
        verify(bookRepository, times(0)).register(any())
    }

    @Test
    @DisplayName("書籍を更新する")
    fun `update when book is exist then update book`() {

        // Given
        val request = UpdateBookRequest(100L, "title", "author", LocalDate.now())
        val json = ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(request)
        val book = Book(request.id, request.title!!, request.author!!, request.releaseDate!!)

        whenever(bookRepository.findWithRental(any())).thenReturn(BookWithRental(book, null))

        // When
        mockMvc.perform(put("/admin/book/update").contentType(APPLICATION_JSON).content(json))
            .andExpect(status().isOk)

        // Then
        verify(bookRepository).update(request.id, request.title, request.author, request.releaseDate)
    }

    @Test
    @DisplayName("書籍IDが無ければ更新しない")
    fun `update when book is not exist then throw Exception`() {

        // Given
        val request = UpdateBookRequest(100L, "title", "author", LocalDate.now())
        val json = ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(request)

        whenever(bookRepository.findWithRental(any())).thenReturn(null)

        // When
        val response = mockMvc.perform(put("/admin/book/update").contentType(APPLICATION_JSON).content(json))
            .andExpect(status().isBadRequest).andReturn().response

        // Then
        Assertions.assertThat(response.errorMessage).isEqualTo("存在しない書籍ID: ${request.id}")
        verify(bookRepository, times(0)).update(any(), any(), any(), any())
    }

    @Test
    @DisplayName("書籍を削除する")
    fun `delete when book is exist then delete it`() {

        // Given
        val bookId = 100L
        val book = Book(bookId, "title", "author", LocalDate.now())

        whenever(bookRepository.findWithRental(any())).thenReturn(BookWithRental(book, null))

        // When
        mockMvc.perform(delete("/admin/book/delete/$bookId")).andExpect(status().isOk)

        // Then
        verify(bookRepository).delete(bookId)
    }

    @Test
    @DisplayName("書籍IDが無ければ削除しない")
    fun `delete when book is not exist then throw Exception`() {

        // Given
        val bookId = 100L

        whenever(bookRepository.findWithRental(any())).thenReturn(null)

        // When
        val response =
            mockMvc.perform(delete("/admin/book/delete/$bookId")).andExpect(status().isBadRequest).andReturn().response

        // Then
        Assertions.assertThat(response.errorMessage).isEqualTo("存在しない書籍ID: $bookId")
        verify(bookRepository, times(0)).delete(bookId)
    }

}