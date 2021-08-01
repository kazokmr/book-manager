package com.book.manager

import com.book.manager.application.service.mockuser.WithCustomMockUser
import com.book.manager.domain.enum.RoleType
import com.book.manager.infrastructure.database.mapper.AccountMapper
import com.book.manager.infrastructure.database.mapper.BookMapper
import com.book.manager.infrastructure.database.mapper.RentalMapper
import com.book.manager.infrastructure.database.mapper.delete
import com.book.manager.infrastructure.database.mapper.insert
import com.book.manager.infrastructure.database.record.AccountRecord
import com.book.manager.infrastructure.database.record.BookRecord
import com.book.manager.infrastructure.database.record.RentalRecord
import com.book.manager.infrastructure.database.testcontainers.TestContainerDataRegistry
import com.book.manager.presentation.form.BookInfo
import com.book.manager.presentation.form.GetBookListResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
internal class BookManagerIntegrationTests(@Autowired val mockMvc: MockMvc) : TestContainerDataRegistry() {

    @Autowired
    private lateinit var accountMapper: AccountMapper

    @Autowired
    private lateinit var bookMapper: BookMapper

    @Autowired
    private lateinit var rentalMapper: RentalMapper

    @BeforeEach
    internal fun setUp() {

        val account1 = AccountRecord(1, "admin@example.com", encode("admin"), "admin", RoleType.ADMIN)
        val account2 = AccountRecord(2, "user@example.com", encode("user"), "user", RoleType.USER)
        val account3 = AccountRecord(1000, "test@example.com", encode("pass"), "test", RoleType.USER)

        accountMapper.insertRecord(account1)
        accountMapper.insertRecord(account2)
        accountMapper.insertRecord(account3)
    }

    @AfterEach
    internal fun tearDown() {
        accountMapper.delete { allRows() }
        bookMapper.delete { allRows() }
        rentalMapper.delete { allRows() }
    }

    private fun encode(password: String) = BCryptPasswordEncoder().encode(password)

    @Test
    @DisplayName("ログインテスト")
    fun `login when user is exist then login`() {
        // Given
        val user = "admin@example.com"
        val pass = "admin"

        // When
        // Then
        mockMvc
            .perform(
                formLogin()
                    .loginProcessingUrl("/login")
                    .user("email", user)
                    .password("pass", pass)
            )
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("書籍リストを取得する")
    @WithCustomMockUser
    fun `bookList when list is exist then return them`() {

        // Given
        val bookInfo1 = BookInfo(100, "Kotlin入門", "ことりん太郎", false)
        val bookInfo2 = BookInfo(200, "Java入門", "じゃば太郎", true)
        val bookInfo3 = BookInfo(300, "Spring入門", "すぷりんぐ太郎", true)
        val bookInfo4 = BookInfo(400, "Kotlin実践", "ことりん太郎", false)

        bookMapper.insert(BookRecord(bookInfo1.id, bookInfo1.title, bookInfo1.author, LocalDate.now()))
        bookMapper.insert(BookRecord(bookInfo2.id, bookInfo2.title, bookInfo2.author, LocalDate.now()))
        bookMapper.insert(BookRecord(bookInfo3.id, bookInfo3.title, bookInfo3.author, LocalDate.now()))
        bookMapper.insert(BookRecord(bookInfo4.id, bookInfo4.title, bookInfo4.author, LocalDate.now()))
        rentalMapper.insert(RentalRecord(bookInfo2.id, 999, LocalDateTime.now(), LocalDateTime.now().plusDays(14)))
        rentalMapper.insert(RentalRecord(bookInfo3.id, 999, LocalDateTime.now(), LocalDateTime.now().plusDays(14)))

        // When
        val resultResponse =
            mockMvc
                .perform(
                    get("/book/list")
                        .with(csrf().asHeader())
                )
                .andExpect(status().isOk)
                .andReturn()
                .response

        // Then
        val result = resultResponse.getContentAsString(StandardCharsets.UTF_8)
            .let {
                ObjectMapper()
                    .registerKotlinModule()
                    .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                    .readValue(it, GetBookListResponse::class.java)
            }
        val expected = GetBookListResponse(listOf(bookInfo1, bookInfo2, bookInfo3, bookInfo4))
        assertThat(result.bookList).containsAll(expected.bookList)
    }

}
