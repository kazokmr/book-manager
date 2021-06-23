package com.book.manager.presentation.controller

import com.book.manager.application.service.RentalService
import com.book.manager.application.service.mockuser.WithCustomMockUser
import com.book.manager.application.service.security.BookManagerUserDetails
import com.book.manager.domain.enum.RoleType
import com.book.manager.domain.model.Account
import com.book.manager.domain.model.Book
import com.book.manager.presentation.form.RentalStartRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate

@WithCustomMockUser
internal class RentalControllerTest {

    private lateinit var mockMvc: MockMvc

    private lateinit var rentalService: RentalService
    private lateinit var rentalController: RentalController

    private lateinit var account: Account
    private lateinit var book: Book

    @BeforeEach
    internal fun setUp() {
        rentalService = mock()
        rentalController = RentalController(rentalService)
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(rentalController)
                .setCustomArgumentResolvers(putAuthenticationPrincipal)
                .build()

        account = Account(1000L, "test@example.com", "pass", "test", RoleType.USER)
        book = Book(1L, "title", "author", LocalDate.now())
    }

    // Controllerクラスの @AuthenticationPrincipalセッションにダミーPrincipalをセットする処理
    private val putAuthenticationPrincipal = object : HandlerMethodArgumentResolver {
        override fun supportsParameter(parameter: MethodParameter) =
            parameter.parameterType.isAssignableFrom(BookManagerUserDetails::class.java)

        override fun resolveArgument(
            parameter: MethodParameter,
            mavContainer: ModelAndViewContainer?,
            webRequest: NativeWebRequest,
            binderFactory: WebDataBinderFactory?
        ) = BookManagerUserDetails(account)
    }

    @Test
    @DisplayName("書籍の貸出し")
    fun `startRental when a book can be rent then start rental`() {

        // Given
        val rentalStartRequest = RentalStartRequest(book.id)
        val json = ObjectMapper().registerKotlinModule().writeValueAsString(rentalStartRequest)

        // When
        mockMvc
            .perform(
                post("/rental/start")
                    .contentType(APPLICATION_JSON)
                    .content(json)
                    .with(csrf().asHeader())
            )
            .andExpect(status().isOk)

        // Then
        verify(rentalService).startRental(book.id, account.id)
    }

    @Test
    @DisplayName("書籍が貸し出せない場合は400 BadRequest エラー")
    fun `startRental when book can not be rent then status is BadRequest`() {

        // Given
        val rentalStartRequest = RentalStartRequest(book.id)
        val json = ObjectMapper().registerKotlinModule().writeValueAsString(rentalStartRequest)
        whenever(rentalService.startRental(any() as Long, any() as Long))
            .thenThrow(IllegalArgumentException::class.java)

        // When
        val exception = mockMvc
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
        assertThat(exception!!::class.java).isEqualTo(ResponseStatusException::class.java)
        assertThat(exception.message).isEqualTo("${HttpStatus.BAD_REQUEST}")
    }

    @Test
    @DisplayName("書籍の返却")
    fun `endRental when return a book then end rental`() {

        // When
        mockMvc
            .perform(
                delete("/rental/end/${book.id}")
                    .with(csrf().asHeader())
            )
            .andExpect(status().isOk)

        // Then
        verify(rentalService).endRental(book.id, account.id)
    }

    @Test
    @DisplayName("書籍が返却できなければ400 BadRequest エラー")
    fun `endRental when an account is not exist then throw Exception`() {

        // Given
        whenever(rentalService.endRental(any() as Long, any() as Long)).thenThrow(IllegalArgumentException::class.java)

        // When
        val exception = mockMvc
            .perform(
                delete("/rental/end/${book.id}")
                    .with(csrf().asHeader())
            )
            .andExpect(status().isBadRequest)
            .andReturn()
            .resolvedException

        // Then
        assertThat(exception!!::class.java).isEqualTo(ResponseStatusException::class.java)
        assertThat(exception.message).isEqualTo("${HttpStatus.BAD_REQUEST}")
    }
}