package com.book.manager.presentation.controller

import com.book.manager.application.service.AuthenticationService
import com.book.manager.application.service.RentalService
import com.book.manager.application.service.mockuser.WithCustomMockUser
import com.book.manager.domain.enum.RoleType
import com.book.manager.domain.model.Account
import com.book.manager.domain.model.Book
import com.book.manager.domain.model.Rental
import com.book.manager.presentation.form.RentalStartRequest
import com.book.manager.presentation.form.RentalStartResponse
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
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.server.ResponseStatusException
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime

@WebMvcTest(controllers = [RentalController::class])
@WithCustomMockUser
internal class RentalControllerTest(@Autowired var mockMvc: MockMvc) {

    @MockBean
    private lateinit var rentalService: RentalService

    @MockBean
    private lateinit var authenticationService: AuthenticationService

    private lateinit var account: Account
    private lateinit var book: Book

    @BeforeEach
    internal fun setUp() {
        account = Account(1000L, "test@example.com", "pass", "test", RoleType.USER)
        book = Book(1L, "title", "author", LocalDate.now())
    }

    @Test
    @DisplayName("書籍の貸出し")
    fun `startRental when a book can be rent then start rental`() {

        // Given
        val rentalStartRequest = RentalStartRequest(book.id)
        val json = ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .registerKotlinModule()
            .writeValueAsString(rentalStartRequest)

        val rentalDate = LocalDateTime.now()
        val rental = Rental(book.id, account.id, rentalDate, rentalDate.plusDays(14))
        whenever(rentalService.startRental(any() as Long, any() as Long)).thenReturn(rental)

        // When
        val resultContent =
            mockMvc
                .perform(
                    post("/rental/start")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .with(csrf().asHeader())
                )
                .andExpect(status().isCreated)
                .andReturn()
                .response
                .getContentAsString(StandardCharsets.UTF_8)

        // Then
        val expectedResponse = RentalStartResponse(rental)
        val expectedContent = ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .registerModule(JavaTimeModule())
            .writeValueAsString(expectedResponse)

        assertThat(resultContent).isEqualTo(expectedContent)
    }

    @Test
    @DisplayName("書籍が貸し出せない場合は HTTP400 BAD_REQUEST")
    fun `startRental when book can not be rent then throw BadRequest`() {

        // Given
        val rentalStartRequest = RentalStartRequest(book.id)
        val json = ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .registerKotlinModule()
            .writeValueAsString(rentalStartRequest)
        val reason = "エラー: bookId ${book.id} accountId ${account.id}"
        whenever(rentalService.startRental(any() as Long, any() as Long)).thenThrow(IllegalArgumentException(reason))

        // When
        val exception =
            mockMvc
                .perform(
                    post("/rental/start")
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
    @DisplayName("書籍IDパラメータが間違っていれば HTTP400 BAD_REQUEST")
    fun `startRental when keyName of bookId is wrong then return BadRequest`() {

        // Given
        val wrongMap = mapOf(Pair("id", 1L))
        val json = ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .registerKotlinModule()
            .writeValueAsString(wrongMap)

        // When
        val resultResponse =
            mockMvc
                .perform(
                    post("/rental/start")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .with(csrf().asHeader())
                )
                .andExpect(status().isBadRequest)
                .andReturn()
                .response

        val result = resultResponse.getContentAsString(StandardCharsets.UTF_8)

        // Then
        val expectedResponse = mapOf(Pair("bookId", "書籍IDには1以上の数値を入れてください。"))
        val expected = ObjectMapper()
            .registerKotlinModule()
            .writeValueAsString(expectedResponse)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("書籍IDが1未満の場合は HTTP400 BAD_REQUEST")
    fun `startRental when bookId is less than 1 then return BadRequest`() {

        // Given
        val rentalStartRequest = RentalStartRequest(0L)
        val json = ObjectMapper()
            .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .registerKotlinModule()
            .writeValueAsString(rentalStartRequest)

        // When
        val resultResponse =
            mockMvc
                .perform(
                    post("/rental/start")
                        .contentType(APPLICATION_JSON)
                        .content(json)
                        .with(csrf().asHeader())
                )
                .andExpect(status().isBadRequest)
                .andReturn()
                .response

        val result = resultResponse.getContentAsString(StandardCharsets.UTF_8)

        // Then
        val expectedResponse = mapOf(Pair("bookId", "書籍IDには1以上の数値を入れてください。"))
        val expected = ObjectMapper()
            .registerKotlinModule()
            .writeValueAsString(expectedResponse)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("書籍の返却")
    fun `endRental when return a book then end rental`() {

        // When
        val resultContent =
            mockMvc
                .perform(
                    delete("/rental/end/${book.id}")
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
    @DisplayName("書籍が返却できなければ HTTP400 BAD_REQUEST")
    fun `endRental when an account is not exist then throw BadRequest`() {

        // Given
        val reason = "エラー: bookId ${book.id} accountId ${account.id}"
        whenever(rentalService.endRental(any() as Long, any() as Long)).thenThrow(IllegalArgumentException(reason))

        // When
        val exception =
            mockMvc
                .perform(
                    delete("/rental/end/${book.id}")
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