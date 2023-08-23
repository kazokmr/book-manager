package com.book.manager.presentation.controller

import com.book.manager.application.service.BookService
import com.book.manager.application.service.mockuser.WithCustomMockUser
import com.book.manager.config.CustomJsonConverter
import com.book.manager.config.CustomTestConfiguration
import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import com.book.manager.domain.model.Rental
import com.book.manager.presentation.form.BookInfo
import com.book.manager.presentation.form.GetBookDetailResponse
import com.book.manager.presentation.form.GetBookListResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.server.ResponseStatusException
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(controllers = [BookController::class])
@Import(CustomTestConfiguration::class)
@WithCustomMockUser
internal class BookControllerTest(@Autowired val mockMvc: MockMvc, @Autowired val jsonConverter: CustomJsonConverter) {

    @MockBean
    private lateinit var bookService: BookService

    private lateinit var testBooks: List<Book>

    @BeforeEach
    internal fun setUp() {
        testBooks = listOf(
            Book(100, "Kotlin入門", "コトリン太郎", LocalDate.now()),
            Book(200, "Java入門", "ジャバ太郎", LocalDate.now()),
            Book(300, "Spring入門", "スプリング太郎", LocalDate.now()),
        )
    }

    @Test
    @DisplayName("書籍リストの取得")
    fun `getList is success`() {

        // Given
        val bookWithRentalList = testBooks.map { BookWithRental(it, null) }
        whenever(bookService.getList()).thenReturn(bookWithRentalList)

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
            .let { jsonConverter.toObject(it, GetBookListResponse::class.java) }
        val expected = GetBookListResponse(bookWithRentalList.map { BookInfo(it) })
        assertThat(result?.bookList).containsExactlyInAnyOrderElementsOf(expected.bookList)
    }

    @Test
    @DisplayName("書籍の詳細情報の取得")
    fun `getDetail is success`() {

        // Given
        val rental = Rental(testBooks[0].id, 1000L, LocalDateTime.now(), LocalDateTime.now().plusDays(14))
        val bookWithRental = BookWithRental(testBooks[0], rental)
        whenever(bookService.getDetail(any() as Long)).thenReturn(bookWithRental)

        // When
        val resultResponse =
            mockMvc
                .perform(
                    get("/book/detail/${testBooks[0].id}")
                        .with(csrf().asHeader())
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
        val reason = "エラー: ${testBooks[0].id}"
        whenever(bookService.getDetail(any() as Long)).thenThrow(IllegalArgumentException(reason))

        // When
        val exception = mockMvc
            .perform(
                get("/book/detail/${testBooks[0].id}")
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
