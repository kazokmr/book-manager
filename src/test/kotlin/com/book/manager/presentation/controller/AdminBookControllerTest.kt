package com.book.manager.presentation.controller

import com.book.manager.application.service.AdminBookService
import com.book.manager.application.service.mockuser.WithCustomMockUser
import com.book.manager.config.CustomJsonConverter
import com.book.manager.config.CustomTestConfiguration
import com.book.manager.domain.enum.RoleType
import com.book.manager.domain.model.Book
import com.book.manager.presentation.form.AdminBookResponse
import com.book.manager.presentation.form.RegisterBookRequest
import com.book.manager.presentation.form.UpdateBookRequest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.server.ResponseStatusException
import java.nio.charset.StandardCharsets
import java.time.LocalDate

@WebMvcTest(controllers = [AdminBookController::class])
@Import(CustomTestConfiguration::class)
@WithCustomMockUser(roleType = RoleType.ADMIN)
internal class AdminBookControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val jsonConverter: CustomJsonConverter
) {

    @MockBean
    private lateinit var adminBookService: AdminBookService

    @Test
    @DisplayName("書籍を登録する")
    fun `register when book of request is not registered then register`() {

        // Given
        val request = RegisterBookRequest(100L, "title", "author", LocalDate.now())
        val json = jsonConverter.toJson(request)

        val book = Book(request.id, request.title, request.author, request.releaseDate)
        whenever(adminBookService.register(any())).thenReturn(book)

        // When
        val resultResponse =
            mockMvc
                .perform(
                    post("/admin/book/register")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .with(csrf().asHeader())
                )
                .andExpect(status().isCreated)
                .andReturn()
                .response

        // Then
        val result = resultResponse.getContentAsString(StandardCharsets.UTF_8)
            .let { jsonConverter.toObject(it, AdminBookResponse::class.java) }
        val expected = AdminBookResponse(book)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("書籍が登録できなければ HTTP400 BAD_REQUEST")
    fun `register when book can not be register then throw BadRequest`() {

        // Given
        val request = RegisterBookRequest(100L, "title", "author", LocalDate.now())
        val json = jsonConverter.toJson(request)
        val reason = "エラー: ${request.id}"

        whenever(adminBookService.register(any())).thenThrow(IllegalArgumentException(reason))

        // When
        val exception =
            mockMvc
                .perform(
                    post("/admin/book/register")
                        .contentType(APPLICATION_JSON)
                        .content(json)
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

    @Test
    @DisplayName("リクエストに必須パラメータが無ければ、書籍は登録できない")
    fun `register when request parameter does not have request item then return exception`() {

        // Given
        val request = mapOf("id" to 100L, "title" to "入門", "author" to "moyo")
        val json = jsonConverter.toJson(request)

        // When
        val result =
            mockMvc
                .perform(
                    post("/admin/book/register")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .with(csrf().asHeader())
                )
                .andExpect(status().isBadRequest)
                .andReturn()

        val response = result.response.getContentAsString(StandardCharsets.UTF_8)

        // Then
        val expected = jsonConverter.toJson(mapOf("入力パラメータがありません" to "release_date"))
        assertThat(response).isEqualTo(expected)
    }

    @Test
    @DisplayName("リクエストパラメータと値の型が異なればエラー")
    fun `register when id is string then throw exception`() {

        // Given
        val request = mapOf("id" to "abc", "title" to "入門", "author" to "moyo", "release_date" to "2018-04-19")
        val json = jsonConverter.toJson(request)

        // When
        val result =
            mockMvc
                .perform(
                    post("/admin/book/register")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .with(csrf().asHeader())
                )
                .andExpect(status().isBadRequest)
                .andReturn()

        val response = result.response.getContentAsString(StandardCharsets.UTF_8)

        // Then
        val expected = jsonConverter.toJson(
            mapOf("入力パラメータの型が一致しません" to "type of id should be long. but value was abc")
        )
        assertThat(response).isEqualTo(expected)
    }

    @Test
    @DisplayName("リクエストパラメータが重複していたら後の値で登録する")
    fun `register when duplicate parameter then register late value`() {

        // Given
        val request =
            mapOf("id" to 123, "id" to 999, "title" to "入門", "author" to "moyo", "release_date" to "2018-04-19")
        val json = jsonConverter.toJson(request)

        val book = Book(999, "入門", "moyo", LocalDate.of(2018, 4, 19))
        whenever(adminBookService.register(any())).thenReturn(book)

        // When
        val resultResponse =
            mockMvc
                .perform(
                    post("/admin/book/register")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .with(csrf().asHeader())
                )
                .andExpect(status().isCreated)
                .andReturn()
                .response

        // Then
        val result = resultResponse.getContentAsString(StandardCharsets.UTF_8)
            .let { jsonConverter.toObject(it, AdminBookResponse::class.java) }
        val expected = AdminBookResponse(book)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("書籍を更新する")
    fun `update when book is exist then update book`() {

        // Given
        val request = UpdateBookRequest(100L, "title", "author", LocalDate.now())
        val json = jsonConverter.toJson(request)

        val book = Book(request.id, request.title!!, request.author!!, request.releaseDate!!)
        whenever(adminBookService.update(any(), any(), any(), any()))
            .thenReturn(request.id)

        // When
        val resultResponse =
            mockMvc
                .perform(
                    put("/admin/book/update")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .with(csrf().asHeader())
                )
                .andExpect(status().isOk)
                .andReturn()
                .response

        // Then
        val result = resultResponse.getContentAsString(StandardCharsets.UTF_8)
            .let { jsonConverter.toObject(it, AdminBookResponse::class.java) }
        val expected = AdminBookResponse(book)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("書籍が更新できなければ HTTP400 BAD_REQUEST")
    fun `update when book can not be update then throw BadRequest`() {

        // Given
        val request = UpdateBookRequest(100L, "title", "author", LocalDate.now())
        val json = jsonConverter.toJson(request)
        val reason = "エラー: ${request.id}"

        whenever(adminBookService.update(any(), any(), any(), any()))
            .thenThrow(IllegalArgumentException(reason))

        // When
        val exception =
            mockMvc
                .perform(
                    put("/admin/book/update")
                        .contentType(APPLICATION_JSON)
                        .content(json)
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

    @Test
    @DisplayName("書籍を削除する")
    fun `delete when book is exist then delete`() {

        // Given
        val bookId = 100L

        // When
        val resultContent =
            mockMvc
                .perform(
                    delete("/admin/book/delete/$bookId")
                        .with(csrf().asHeader())
                )
                .andExpect(status().isNoContent)
                .andReturn()
                .response
                .getContentAsString(StandardCharsets.UTF_8)

        // Then
        assertThat(resultContent).isEmpty()
    }

    @Test
    @DisplayName("書籍が削除できなければ HTTP400 BAD_REQUEST")
    fun `delete when book can not be delete then throw BadRequest`() {

        // Given
        val bookId = 100L
        val reason = "エラー: $bookId"
        whenever(adminBookService.delete(any())).thenThrow(IllegalArgumentException(reason))

        // When
        val exception =
            mockMvc
                .perform(
                    delete("/admin/book/delete/$bookId")
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
