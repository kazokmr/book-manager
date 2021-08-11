package com.book.manager

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
import com.book.manager.presentation.config.IntegrationTestConfiguration
import com.book.manager.presentation.config.IntegrationTestRestTemplate
import com.book.manager.presentation.form.BookInfo
import com.book.manager.presentation.form.GetBookListResponse
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration::class)
internal class BookManagerIntegrationTests : TestContainerDataRegistry() {

    @Autowired
    private lateinit var restTemplate: IntegrationTestRestTemplate

    @LocalServerPort
    private var port: Int = 0

    @Autowired
    private lateinit var accountMapper: AccountMapper

    @Autowired
    private lateinit var bookMapper: BookMapper

    @Autowired
    private lateinit var rentalMapper: RentalMapper


    @BeforeEach
    internal fun setUp() {

        val account1 = AccountRecord(1, "admin@example.com", "admin".encode(), "admin", RoleType.ADMIN)
        val account2 = AccountRecord(2, "user@example.com", "user".encode(), "user", RoleType.USER)
        val account3 = AccountRecord(1000, "test@example.com", "pass".encode(), "test", RoleType.USER)

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

    private fun String.encode() = BCryptPasswordEncoder().encode(this)

    @Test
    @DisplayName("ログインテスト")
    fun `login when user is exist then login`() {

        // Given
        val user = "admin@example.com"
        val pass = "admin"

        // When
        val response = restTemplate.login(port, user, pass)

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    @DisplayName("書籍リストを取得する")
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

        val user = "admin@example.com"
        val pass = "admin"
        restTemplate.login(port, user, pass)

        // When
        val response = restTemplate.getForEntity("http://localhost:$port/book/list", GetBookListResponse::class.java)

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val result = response.body
        val expected = GetBookListResponse(listOf(bookInfo1, bookInfo2, bookInfo3, bookInfo4))
        assertThat(result?.bookList).containsExactlyInAnyOrderElementsOf(expected.bookList)
    }
}
