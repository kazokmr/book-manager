package com.book.manager.presentation.controller

import com.book.manager.application.service.BookService
import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import com.book.manager.domain.model.Rental
import com.book.manager.domain.repository.BookRepository
import com.book.manager.presentation.form.BookInfo
import com.book.manager.presentation.form.GetBookDetailResponse
import com.book.manager.presentation.form.GetBookListResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime

internal class BookControllerTest {

    private val bookService = mock<BookService>()
    private val bookController = BookController(bookService)

    @Test
    @DisplayName("書籍リストの取得")
    fun `getList is success`() {
        val bookId = 100L
        val book = Book(bookId, "Kotlin入門", "コトリン太郎", LocalDate.now())
        val bookList = listOf(BookWithRental(book, null))

        whenever(bookService.getList()).thenReturn(bookList)

        val expectedResponse = GetBookListResponse(listOf(BookInfo(bookId, "Kotlin入門", "コトリン太郎", false)))
        val expected = ObjectMapper().registerKotlinModule().writeValueAsString(expectedResponse)
        val mockMvc = MockMvcBuilders.standaloneSetup(bookController).build()
        val resultResponse = mockMvc.perform(get("/book/list")).andExpect(status().isOk).andReturn().response
        val result = resultResponse.getContentAsString(StandardCharsets.UTF_8)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("書籍の詳細情報の取得")
    fun `getDetail is success`() {

        val book = Book(100L, "Kotlin入門", "コトリン太郎", LocalDate.now())
        val rental = Rental(book.id, 1000L, LocalDateTime.now(), LocalDateTime.now().plusDays(14))
        val bookWithRental = BookWithRental(book, rental)
        whenever(bookService.getDetail(book.id)).thenReturn(bookWithRental)

        val expectedResponse = GetBookDetailResponse(bookWithRental)
        // LocalDateTimeを利用するためにJavaTimeModuleを登録する(これはKotlinModuleだと登録できない)
        val expected = ObjectMapper().registerModule(JavaTimeModule()).writeValueAsString(expectedResponse)

        val mockMvc = MockMvcBuilders.standaloneSetup(bookController).build()
        val resultResponse =
            mockMvc.perform(get("/book/detail/${book.id}")).andExpect(status().isOk).andReturn().response
        val result = resultResponse.getContentAsString(StandardCharsets.UTF_8)

        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("書籍情報が取得できない場合")
    fun `getDetail when book is not exists then throw Exception`() {

        val bookRepository = mock<BookRepository>()
        val bookService = BookService(bookRepository)
        val bookController = BookController(bookService)
        whenever(bookRepository.findWithRental(any())).thenReturn(null)
        val bookId = 100L

        val mockMvc = MockMvcBuilders.standaloneSetup(bookController).build()

        val response =
            mockMvc.perform(get("/book/detail/$bookId")).andExpect(status().isBadRequest).andReturn().response
        assertThat(response.errorMessage).isEqualTo("存在しない書籍ID: $bookId")
    }
}