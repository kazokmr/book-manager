package com.book.manager

import com.book.manager.config.CustomJsonConverter
import com.book.manager.config.CustomTestConfiguration
import com.book.manager.config.CustomTestMapper
import com.book.manager.config.IntegrationTestConfiguration
import com.book.manager.config.IntegrationTestRestTemplate
import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import com.book.manager.domain.model.Rental
import com.book.manager.infrastructure.database.testcontainers.TestContainerDataRegistry
import com.book.manager.presentation.form.AdminBookResponse
import com.book.manager.presentation.form.BookInfo
import com.book.manager.presentation.form.GetBookDetailResponse
import com.book.manager.presentation.form.GetBookListResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.json.JSONObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration::class, CustomTestConfiguration::class, CustomTestMapper::class)
internal class BookManagerIntegrationTests : TestContainerDataRegistry() {

    @Autowired
    private lateinit var restTemplate: IntegrationTestRestTemplate

    @Autowired
    private lateinit var jsonConverter: CustomJsonConverter

    @Autowired
    private lateinit var testMapper: CustomTestMapper

    @LocalServerPort
    private var port: Int = 0

    @BeforeEach
    internal fun setUp() {
        testMapper.initDefaultAccounts()
    }

    @AfterEach
    internal fun tearDown() {
        testMapper.clearAllData()
    }

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
        val testBookWithRentalList =
            listOf(
                BookWithRental(
                    Book(bookInfo1.id, bookInfo1.title, bookInfo1.author, LocalDate.now()),
                    null
                ),
                BookWithRental(
                    Book(bookInfo2.id, bookInfo2.title, bookInfo2.author, LocalDate.now()),
                    Rental(bookInfo2.id, 1000, LocalDateTime.now(), LocalDateTime.now().plusDays(14))
                ),
                BookWithRental(
                    Book(bookInfo3.id, bookInfo3.title, bookInfo3.author, LocalDate.now()),
                    Rental(bookInfo3.id, 1000, LocalDateTime.now(), LocalDateTime.now().plusDays(14))
                ),
                BookWithRental(
                    Book(bookInfo4.id, bookInfo4.title, bookInfo4.author, LocalDate.now()),
                    null
                )
            )
        testMapper.createBookWithRental(testBookWithRentalList)

        val user = "admin@example.com"
        val pass = "admin"
        restTemplate.login(port, user, pass)

        // When
        val response = restTemplate.getForEntity("http://localhost:$port/book/list", String::class.java)

        // Then
        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        val result = jsonConverter.toObject(response.body, GetBookListResponse::class.java)
        val expected = GetBookListResponse(listOf(bookInfo1, bookInfo2, bookInfo3, bookInfo4))
        assertThat(result?.bookList).containsExactlyInAnyOrderElementsOf(expected.bookList)
    }

    @Test
    @DisplayName("書籍を登録する")
    fun `when book is register then get this`() {

        // Given
        val user = "admin@example.com"
        val pass = "admin"
        restTemplate.login(port, user, pass)

        // When
        val book = Book(789, "統合テスト", "テスト二郎", LocalDate.of(2010, 12, 3))
        val httpHeaders = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val jsonObject = JSONObject().apply {
            put("id", book.id)
            put("title", book.title)
            put("author", book.author)
            put("release_date", book.releaseDate.format(DateTimeFormatter.ISO_DATE))
        }

        val postRequest = HttpEntity<String>(jsonObject.toString(), httpHeaders)
        val postResponse = restTemplate.postForEntity(
            "http://localhost:$port/admin/book/register",
            postRequest,
            String::class.java
        )
        val registeredBook = jsonConverter.toObject(postResponse.body, AdminBookResponse::class.java)

        // Then
        val response = restTemplate.getForEntity(
            "http://localhost:$port/book/detail/${book.id}",
            String::class.java
        )
        val result = jsonConverter.toObject(response.body, GetBookDetailResponse::class.java)
        val expected = GetBookDetailResponse(BookWithRental(book, null))

        SoftAssertions().apply {
            assertThat(postResponse.statusCode).isEqualTo(HttpStatus.CREATED)
            assertThat(registeredBook?.id).isEqualTo(book.id)
            assertThat(registeredBook?.title).isEqualTo(book.title)
            assertThat(registeredBook?.author).isEqualTo(book.author)
            assertThat(registeredBook?.releaseDate).isEqualTo(book.releaseDate)
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(result).isEqualTo(expected)
        }.assertAll()
    }
}
