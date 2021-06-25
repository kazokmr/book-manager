package com.book.manager.presentation.controller

import com.book.manager.application.service.AdminBookService
import com.book.manager.application.service.AuthenticationService
import com.book.manager.application.service.mockuser.WithCustomMockUser
import com.book.manager.application.service.result.Result
import com.book.manager.domain.enum.RoleType
import com.book.manager.domain.model.Book
import com.book.manager.presentation.form.RegisterBookRequest
import com.book.manager.presentation.form.UpdateBookRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

@WebMvcTest(controllers = [AdminBookController::class])
@WithCustomMockUser(roleType = RoleType.ADMIN)
internal class AdminBookControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockBean
    private lateinit var adminBookService: AdminBookService

    @MockBean
    private lateinit var authenticationService: AuthenticationService

    @Test
    @DisplayName("書籍を登録する")
    fun `register when book of request is not registered then register`() {

        // Given
        val request = RegisterBookRequest(100L, "title", "author", LocalDate.now())
        val json = ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .registerModule(JavaTimeModule())
            .writeValueAsString(request)

        // When
        mockMvc
            .perform(
                post("/admin/book/register")
                    .contentType(APPLICATION_JSON)
                    .content(json)
                    .with(csrf().asHeader())
            )
            .andExpect(status().isOk)

        // Then
        val book = Book(request.id, request.title, request.author, request.releaseDate)
        verify(adminBookService).register(book)
    }

    @Test
    @DisplayName("書籍が登録できなければ HTTP400 BAD_REQUEST")
    fun `register when book can not be register then throw BadRequest`() {

        // Given
        val request = RegisterBookRequest(100L, "title", "author", LocalDate.now())
        val json =
            ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .registerModule(JavaTimeModule())
                .writeValueAsString(request)
        val reason = "エラー: ${request.id}"

        whenever(adminBookService.register(any() as Book)).thenReturn(Result.Failure(reason))

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
    @DisplayName("書籍を更新する")
    fun `update when book is exist then update book`() {

        // Given
        val request = UpdateBookRequest(100L, "title", "author", LocalDate.now())
        val json =
            ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .registerModule(JavaTimeModule())
                .writeValueAsString(request)

        // When
        mockMvc
            .perform(
                put("/admin/book/update")
                    .contentType(APPLICATION_JSON)
                    .content(json)
                    .with(csrf().asHeader())
            )
            .andExpect(status().isOk)

        // Then
        verify(adminBookService).update(request.id, request.title, request.author, request.releaseDate)
    }

    @Test
    @DisplayName("書籍が更新できなければ HTTP400 BAD_REQUEST")
    fun `update when book can not be update then throw BadRequest`() {

        // Given
        val request = UpdateBookRequest(100L, "title", "author", LocalDate.now())
        val json =
            ObjectMapper()
                .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                .registerModule(JavaTimeModule())
                .writeValueAsString(request)
        val reason = "エラー: ${request.id}"

        whenever(adminBookService.update(any() as Long, any() as String, any() as String, any() as LocalDate))
            .thenReturn(Result.Failure(reason))

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
        mockMvc
            .perform(
                delete("/admin/book/delete/$bookId")
                    .with(csrf().asHeader())
            )
            .andExpect(status().isOk)

        // Then
        verify(adminBookService).delete(bookId)
    }

    @Test
    @DisplayName("書籍が削除できなければ HTTP400 BAD_REQUEST")
    fun `delete when book can not be delete then throw BadRequest`() {

        // Given
        val bookId = 100L
        val reason = "エラー: $bookId"
        whenever(adminBookService.delete(any() as Long)).thenReturn(Result.Failure(reason))

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