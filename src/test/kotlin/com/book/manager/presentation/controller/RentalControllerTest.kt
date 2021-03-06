package com.book.manager.presentation.controller

import com.book.manager.application.service.RentalService
import com.book.manager.application.service.mockuser.WithCustomMockUser
import com.book.manager.config.CustomJsonConverter
import com.book.manager.config.CustomTestConfiguration
import com.book.manager.domain.enum.RoleType
import com.book.manager.domain.model.Account
import com.book.manager.domain.model.Book
import com.book.manager.domain.model.Rental
import com.book.manager.presentation.form.RentalStartRequest
import com.book.manager.presentation.form.RentalStartResponse
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
@Import(CustomTestConfiguration::class)
internal class RentalControllerTest(
    @Autowired val mockMvc: MockMvc,
    @Autowired val jsonConverter: CustomJsonConverter
) {

    @MockBean
    private lateinit var rentalService: RentalService

    private lateinit var account: Account
    private lateinit var book: Book

    @BeforeEach
    internal fun setUp() {
        account = Account(1000L, "test@example.com", "pass", "test", RoleType.USER)
        book = Book(1L, "title", "author", LocalDate.now())
    }

    @Test
    @DisplayName("??????????????????")
    fun `startRental when a book can be rent then start rental`() {

        // Given
        val request = RentalStartRequest(book.id)
        val json = jsonConverter.toJson(request)

        val rentalDate = LocalDateTime.now()
        val rental = Rental(book.id, account.id, rentalDate, rentalDate.plusDays(14))
        whenever(rentalService.startRental(any() as Long, any() as Long)).thenReturn(rental)

        // When
        val resultResponse =
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

        // Then
        val result = resultResponse.getContentAsString(StandardCharsets.UTF_8)
            .let { jsonConverter.toObject(it, RentalStartResponse::class.java) }
        val expected = RentalStartResponse(rental)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("???????????????????????????????????? HTTP400 BAD_REQUEST")
    fun `startRental when book can not be rent then throw BadRequest`() {

        // Given
        val request = RentalStartRequest(book.id)
        val json = jsonConverter.toJson(request)
        val reason = "?????????: bookId ${book.id} accountId ${account.id}"
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
    @DisplayName("??????ID??????????????????????????????????????? HTTP400 BAD_REQUEST")
    fun `startRental when keyName of bookId is wrong then return BadRequest`() {

        // Given
        val wrongMap = mapOf(Pair("id", 1L))
        val json = jsonConverter.toJson(wrongMap)

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
        val expectedResponse = mapOf(Pair("bookId", "??????ID??????1??????????????????????????????????????????"))
        val expected = jsonConverter.toJson(expectedResponse)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("??????ID???1?????????????????? HTTP400 BAD_REQUEST")
    fun `startRental when bookId is less than 1 then return BadRequest`() {

        // Given
        val request = RentalStartRequest(0L)
        val json = jsonConverter.toJson(request)

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
        val expectedResponse = mapOf(Pair("bookId", "??????ID??????1??????????????????????????????????????????"))
        val expected = jsonConverter.toJson(expectedResponse)
        assertThat(result).isEqualTo(expected)
    }

    @Test
    @DisplayName("???????????????")
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
    @DisplayName("????????????????????????????????? HTTP400 BAD_REQUEST")
    fun `endRental when an account is not exist then throw BadRequest`() {

        // Given
        val reason = "?????????: bookId ${book.id} accountId ${account.id}"
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