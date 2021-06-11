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
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.LocalDate

internal class AdminBookControllerTest {

    private val adminBookService = mock<AdminBookService>()
    private val adminBookController = AdminBookController(adminBookService)
    private val mockMvc = MockMvcBuilders.standaloneSetup(adminBookController).build()

    @Test
    @DisplayName("書籍を登録する")
    fun `register when book of request is not registered then the book is done`() {

        val request = RegisterBookRequest(100L, "title", "author", LocalDate.now())
        val json = ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(request)
        val book = Book(request.id, request.title, request.author, request.releaseDate)

        mockMvc.perform(post("/admin/book/register").contentType(APPLICATION_JSON).content(json))
            .andExpect(status().isOk)

        verify(adminBookService).register(book)
    }

    @Test
    @DisplayName("書籍IDが登録済みなら登録しない")
    fun `register when book is exist then throw Exception`() {
        val bookRepository = mock<BookRepository>()
        val adminBookService = AdminBookService(bookRepository)
        val adminBookController = AdminBookController(adminBookService)

        val request = RegisterBookRequest(100L, "title", "author", LocalDate.now())
        val json = ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(request)
        val book = Book(request.id, request.title, request.author, request.releaseDate)

        whenever(bookRepository.findWithRental(any())).thenReturn(BookWithRental(book, null))

        val mockMvc = MockMvcBuilders.standaloneSetup(adminBookController).build()
        val response = mockMvc.perform(post("/admin/book/register").contentType(APPLICATION_JSON).content(json))
            .andExpect(status().isBadRequest).andReturn().response

        Assertions.assertThat(response.errorMessage).isEqualTo("既に存在する書籍ID: ${book.id}")
        verify(bookRepository, times(0)).register(any())
    }

    @Test
    @DisplayName("書籍を更新する")
    fun `update when book is exist then update book`() {

        val request = UpdateBookRequest(100L, "title", "author", LocalDate.now())
        val json = ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(request)

        mockMvc.perform(put("/admin/book/update").contentType(APPLICATION_JSON).content(json))
            .andExpect(status().isOk)

        verify(adminBookService).update(request.id, request.title, request.author, request.releaseDate)
    }

    @Test
    @DisplayName("書籍IDが無ければ更新しない")
    fun `update when book is not exist then throw Exception`() {
        val bookRepository = mock<BookRepository>()
        val adminBookService = AdminBookService(bookRepository)
        val adminBookController = AdminBookController(adminBookService)

        val request = UpdateBookRequest(100L, "title", "author", LocalDate.now())
        val json = ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(request)

        whenever(bookRepository.findWithRental(any())).thenReturn(null)

        val mockMvc = MockMvcBuilders.standaloneSetup(adminBookController).build()
        val response = mockMvc.perform(put("/admin/book/update").contentType(APPLICATION_JSON).content(json))
            .andExpect(status().isBadRequest).andReturn().response

        Assertions.assertThat(response.errorMessage).isEqualTo("存在しない書籍ID: ${request.id}")
        verify(bookRepository, times(0)).update(any(), any(), any(), any())
    }

    @Test
    @DisplayName("書籍を削除する")
    fun `delete when book is exist then delete it`() {
        val bookId = 100L
        mockMvc.perform(delete("/admin/book/delete/$bookId")).andExpect(status().isOk)
        verify(adminBookService).delete(bookId)
    }

    @Test
    @DisplayName("書籍IDが無ければ削除しない")
    fun `delete when book is not exist then throw Exception`() {
        val bookRepository = mock<BookRepository>()
        val adminBookService = AdminBookService(bookRepository)
        val adminBookController = AdminBookController(adminBookService)
        val bookId = 100L

        whenever(bookRepository.findWithRental(any())).thenReturn(null)

        val mockMvc = MockMvcBuilders.standaloneSetup(adminBookController).build()
        val response =
            mockMvc.perform(delete("/admin/book/delete/$bookId")).andExpect(status().isBadRequest).andReturn().response

        Assertions.assertThat(response.errorMessage).isEqualTo("存在しない書籍ID: $bookId")
        verify(bookRepository, times(0)).delete(bookId)
    }

}