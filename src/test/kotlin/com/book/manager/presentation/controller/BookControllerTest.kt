package com.book.manager.presentation.controller

import com.book.manager.application.service.BookService
import com.book.manager.config.CustomJsonConverter
import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import com.book.manager.domain.model.Rental
import com.book.manager.presentation.form.BookInfo
import com.book.manager.presentation.form.GetBookDetailResponse
import com.book.manager.presentation.form.GetBookListResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.server.ResponseStatusException
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(controllers = [BookController::class])
@WithMockUser(authorities = ["USER"])
internal class BookControllerTest(
    @Autowired private val mockMvc: MockMvc,
) {

    @MockitoBean
    private lateinit var bookService: BookService

    private val jsonConverter: CustomJsonConverter = CustomJsonConverter()

    @Test
    @DisplayName("書籍リストの取得")
    fun `getList is success`() {

        // Given
        val bookWithRentalList = listOf(
            BookWithRental(Book(100, "Kotlin入門", "コトリン太郎", LocalDate.now()), null),
            BookWithRental(Book(200, "Java入門", "ジャバ太郎", LocalDate.now()), null),
            BookWithRental(Book(300, "Spring入門", "スプリング太郎", LocalDate.now()), null)
        )
        doReturn(bookWithRentalList).`when`(bookService).getList()

        // When
        val resultResponse =
            mockMvc
                .perform(
                    get("/book/list")
                )
                .andExpect(status().isOk)
                .andReturn().response

        // Then
        val result = resultResponse.getContentAsString(StandardCharsets.UTF_8)
            .let { jsonConverter.toObject(it, GetBookListResponse::class.java) }
        val expected = GetBookListResponse(bookWithRentalList.map { BookInfo(it) })
        assertThat(result?.bookList).containsExactlyInAnyOrderElementsOf(expected.bookList)
    }

    @Test
    @DisplayName("書籍の詳細情報の取得")
    fun `getDetail is success`() {

        // Given
        val book = Book(100, "Kotlin入門", "コトリン太郎", LocalDate.now())
        val rental = Rental(100, 1000, LocalDateTime.now(), LocalDateTime.now().plusDays(14))
        val bookWithRental = BookWithRental(book, rental)
        doReturn(bookWithRental).`when`(bookService).getDetail(any())

        // When
        val resultResponse =
            mockMvc
                .perform(
                    get("/book/detail/100")
                )
                .andExpect(status().isOk)
                .andReturn()
                .response

        // Then
        // LocalDateTimeを利用するためにJavaTimeModuleを追加定義する
        val result = resultResponse.getContentAsString(StandardCharsets.UTF_8)
            .let { jsonConverter.toObject(it, GetBookDetailResponse::class.java) }
        val expected = GetBookDetailResponse(bookWithRental)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("書籍情報が取得できない場合は HTTP400 BAD_REQUEST")
    fun `getDetail when book is not exists then throw BadRequest`() {

        // Given
        val reason = "エラー: 100"
        doThrow(IllegalArgumentException(reason)).`when`(bookService).getDetail(any())

        // When
        val exception = mockMvc
            .perform(
                get("/book/detail/100")
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
