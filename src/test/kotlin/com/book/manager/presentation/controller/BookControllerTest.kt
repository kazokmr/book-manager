package com.book.manager.presentation.controller

import com.book.manager.application.service.AuthenticationService
import com.book.manager.application.service.BookService
import com.book.manager.application.service.mockuser.WithCustomMockUser
import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import com.book.manager.domain.model.Rental
import com.book.manager.presentation.form.BookInfo
import com.book.manager.presentation.form.GetBookDetailResponse
import com.book.manager.presentation.form.GetBookListResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.server.ResponseStatusException
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(controllers = [BookController::class])
@WithCustomMockUser
internal class BookControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockBean
    private lateinit var bookService: BookService

    @MockBean
    private lateinit var authenticationService: AuthenticationService

    private lateinit var book: Book

    @BeforeEach
    internal fun setUp() {
        book = Book(100L, "Kotlin入門", "コトリン太郎", LocalDate.now())
    }

    @Test
    @DisplayName("書籍リストの取得")
    fun `getList is success`() {

        // Given
        val bookList = listOf(BookWithRental(book, null))
        whenever(bookService.getList()).thenReturn(bookList)

        // When
        val resultResponse =
            mockMvc
                .perform(
                    get("/book/list")
                        .with(csrf().asHeader())
                )
                .andExpect(status().isOk)
                .andReturn().response

        // Then
        val result = resultResponse.getContentAsString(StandardCharsets.UTF_8)
        val expectedResponse = GetBookListResponse(listOf(BookInfo(BookWithRental(book, null))))
        val expected = ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .registerKotlinModule()
            .writeValueAsString(expectedResponse)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("書籍の詳細情報の取得")
    fun `getDetail is success`() {

        // Given
        val rental = Rental(book.id, 1000L, LocalDateTime.now(), LocalDateTime.now().plusDays(14))
        val bookWithRental = BookWithRental(book, rental)

        whenever(bookService.getDetail(any() as Long)).thenReturn(bookWithRental)

        // When
        val resultResponse =
            mockMvc
                .perform(
                    get("/book/detail/${book.id}")
                        .with(csrf().asHeader())
                )
                .andExpect(status().isOk)
                .andReturn()
                .response
        val result = resultResponse.getContentAsString(StandardCharsets.UTF_8)

        // Then
        val expectedResponse = GetBookDetailResponse(bookWithRental)
        // LocalDateTimeを利用するためにJavaTimeModuleを登録する(これはKotlinModuleだと登録できない)
        val expected = ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .registerModule(JavaTimeModule())
            .writeValueAsString(expectedResponse)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("書籍情報が取得できない場合は HTTP400 BAD_REQUEST")
    fun `getDetail when book is not exists then throw BadRequest`() {

        // Given
        val reason = "エラー: ${book.id}"
        whenever(bookService.getDetail(any() as Long)).thenThrow(IllegalArgumentException(reason))

        // When
        val exception = mockMvc
            .perform(
                get("/book/detail/${book.id}")
                    .with(csrf().asHeader())
            )
            .andExpect(status().isBadRequest)
            .andReturn()
            .resolvedException

        // Then
        assertThat(exception).isInstanceOf(ResponseStatusException::class.java)
        exception as ResponseStatusException
        assertThat(exception.reason).isEqualTo(reason)
    }
}