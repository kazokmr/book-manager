package com.book.manager.presentation.controller

import com.book.manager.application.service.RentalService
import com.book.manager.application.service.mockuser.WithCustomMockUser
import com.book.manager.application.service.security.BookManagerUserDetails
import com.book.manager.domain.enum.RoleType
import com.book.manager.domain.model.Account
import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import com.book.manager.domain.model.Rental
import com.book.manager.domain.repository.AccountRepository
import com.book.manager.domain.repository.BookRepository
import com.book.manager.domain.repository.RentalRepository
import com.book.manager.presentation.form.RentalStartRequest
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
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
import java.time.LocalDate
import java.time.LocalDateTime

@WithCustomMockUser
internal class RentalControllerTest {

    private lateinit var mockMvc: MockMvc

    private lateinit var accountRepository: AccountRepository
    private lateinit var bookRepository: BookRepository
    private lateinit var rentalRepository: RentalRepository
    private lateinit var rentalService: RentalService
    private lateinit var rentalController: RentalController

    private lateinit var account: Account
    private lateinit var book: Book

    @BeforeEach
    internal fun setUp() {
        accountRepository = mock()
        bookRepository = mock()
        rentalRepository = mock()
        rentalService = RentalService(accountRepository, bookRepository, rentalRepository)
        rentalController = RentalController(rentalService)
        mockMvc =
            MockMvcBuilders
                .standaloneSetup(rentalController)
                .setCustomArgumentResolvers(putAuthenticationPrincipal)
                .build()
        account = Account(1000L, "test@example.com", "pass", "test", RoleType.USER)
        book = Book(1L, "title", "author", LocalDate.now())
    }

    @Test
    @DisplayName("書籍の貸出")
    fun `startRental when a book can be rent then start rental`() {

        // Given
        val rentalStartRequest = RentalStartRequest(book.id)
        val json = ObjectMapper().registerKotlinModule().writeValueAsString(rentalStartRequest)
        val bookWithRental = BookWithRental(book, null)

        whenever(accountRepository.findById(any())).thenReturn(account)
        whenever(bookRepository.findWithRental(any())).thenReturn(bookWithRental)

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
        verify(rentalRepository).startRental(any())
    }

    @Test
    @DisplayName("アカウントが存在しなければ貸出不可")
    fun `startRental when an account is not exist then throw Exception`() {

        // Given
        val rentalStartRequest = RentalStartRequest(book.id)
        val json = ObjectMapper().registerKotlinModule().writeValueAsString(rentalStartRequest)

        whenever(accountRepository.findById(any())).thenReturn(null)

        // When
        val response = mockMvc.perform(post("/rental/start").contentType(APPLICATION_JSON).content(json))
            .andExpect(status().isBadRequest).andReturn().response

        // Then
        assertThat(response.errorMessage).isEqualTo("該当するユーザーが存在しません accountId: ${account.id}")
        verify(bookRepository, times(0)).findWithRental(any())
        verify(rentalRepository, times(0)).startRental(any())
    }

    @Test
    @DisplayName("書籍が存在しなければ貸出不可")
    fun `startRental when a book is not exist then throw Exception`() {

        // Given
        val rentalStartRequest = RentalStartRequest(book.id)
        val json = ObjectMapper().registerKotlinModule().writeValueAsString(rentalStartRequest)

        whenever(accountRepository.findById(any())).thenReturn(account)
        whenever(bookRepository.findWithRental(any())).thenReturn(null)

        // When
        val response = mockMvc.perform(post("/rental/start").contentType(APPLICATION_JSON).content(json))
            .andExpect(status().isBadRequest).andReturn().response

        // Then
        assertThat(response.errorMessage).isEqualTo("該当する書籍が存在しません bookId: ${book.id}")
        verify(rentalRepository, times(0)).startRental(any())
    }

    @Test
    @DisplayName("書籍が貸出中なら貸出不可")
    fun `startRental when the book is rent then throw Exception`() {

        // Given
        val rentalStartRequest = RentalStartRequest(book.id)
        val json = ObjectMapper().registerKotlinModule().writeValueAsString(rentalStartRequest)

        whenever(accountRepository.findById(any())).thenReturn(account)
        val rental = Rental(book.id, account.id, LocalDateTime.now(), LocalDateTime.now().plusDays(14))
        whenever(bookRepository.findWithRental(any())).thenReturn(BookWithRental(book, rental))

        // When
        val response = mockMvc.perform(post("/rental/start").contentType(APPLICATION_JSON).content(json))
            .andExpect(status().isBadRequest).andReturn().response

        // Then
        assertThat(response.errorMessage).isEqualTo("貸出中の書籍です bookId: ${book.id}")
        verify(rentalRepository, times(0)).startRental(any())
    }

    @Test
    @DisplayName("書籍の返却")
    fun `endRental when return a book then end rental`() {

        // Given
        val rental = Rental(book.id, account.id, LocalDateTime.now(), LocalDateTime.now().plusDays(14))
        whenever(accountRepository.findById(any())).thenReturn(account)
        whenever(bookRepository.findWithRental(any())).thenReturn(BookWithRental(book, rental))

        // When
        mockMvc.perform(delete("/rental/end/${book.id}")).andExpect(status().isOk)

        // Then
        verify(rentalRepository).endRental(book.id)
    }

    @Test
    @DisplayName("アカウントが存在しなければ返却不可")
    fun `endRental when an account is not exist then throw Exception`() {

        // Given
        whenever(accountRepository.findById(any())).thenReturn(null)

        // When
        val response =
            mockMvc.perform(delete("/rental/end/${book.id}")).andExpect(status().isBadRequest).andReturn().response

        // Then
        assertThat(response.errorMessage).isEqualTo("該当するユーザーが存在しません accountId: ${account.id}")
        verify(bookRepository, times(0)).findWithRental(any())
        verify(rentalRepository, times(0)).endRental(any())
    }

    @Test
    @DisplayName("書籍が存在しなければ返却不可")
    fun `endReturn when a book is not exist then throw Exception`() {
        // Given
        whenever(accountRepository.findById(any())).thenReturn(account)
        whenever(bookRepository.findWithRental(any())).thenReturn(null)

        // When
        val response =
            mockMvc.perform(delete("/rental/end/${book.id}")).andExpect(status().isBadRequest).andReturn().response

        // Then
        assertThat(response.errorMessage).isEqualTo("該当する書籍が存在しません bookId: ${book.id}")
        verify(rentalRepository, times(0)).endRental(any())
    }

    @Test
    @DisplayName("書籍が貸し出されていなければ、返却不可")
    fun `endReturn when a book is not rent then throw Exception`() {

        // Given
        whenever(accountRepository.findById(any())).thenReturn(account)
        whenever(bookRepository.findWithRental(any())).thenReturn(BookWithRental(book, null))

        // When
        val response =
            mockMvc.perform(delete("/rental/end/${book.id}")).andExpect(status().isBadRequest).andReturn().response

        // Then
        assertThat(response.errorMessage).isEqualTo("未貸出の書籍です bookId: ${book.id}")
        verify(rentalRepository, times(0)).endRental(any())
    }

    @Test
    @DisplayName("借りているユーザーが異なれば、返却不可")
    fun `endReturn when an account is different then throw Exception`() {

        // Given
        val rental = Rental(book.id, account.id + 10L, LocalDateTime.now(), LocalDateTime.now().plusDays(14))
        whenever(accountRepository.findById(any())).thenReturn(account)
        whenever(bookRepository.findWithRental(any())).thenReturn(BookWithRental(book, rental))

        // When
        val response =
            mockMvc.perform(delete("/rental/end/${book.id}")).andExpect(status().isBadRequest).andReturn().response

        // Then
        assertThat(response.errorMessage).isEqualTo("他のユーザーが貸出中の商品です accountId: ${account.id}, bookId: ${book.id}")
        verify(rentalRepository, times(0)).endRental(any())
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
}