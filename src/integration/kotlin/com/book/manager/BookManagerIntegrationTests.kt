package com.book.manager

import com.book.manager.config.CustomJsonConverter
import com.book.manager.config.CustomTestConfiguration
import com.book.manager.config.CustomTestMapper
import com.book.manager.config.IntegrationTestConfiguration
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Import
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Stream

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration::class, CustomTestConfiguration::class, CustomTestMapper::class)
internal class BookManagerIntegrationTests : TestContainerDataRegistry() {

    @Autowired
    private lateinit var builder: RestTemplateBuilder

    @Autowired
    private lateinit var jsonConverter: CustomJsonConverter

    @Autowired
    private lateinit var testMapper: CustomTestMapper

    @LocalServerPort
    private var port: Int = 0

    private lateinit var baseUri: String
    private lateinit var restTemplate: TestRestTemplate

    @BeforeEach
    internal fun setUp() {
        baseUri = "http://localhost:$port"
        restTemplate = TestRestTemplate(builder)
        testMapper.initDefaultAccounts()
    }

    @AfterEach
    internal fun tearDown() {
        testMapper.clearAllData()
    }

    companion object {
        @JvmStatic
        fun users(): Stream<Arguments> = Stream.of(
            Arguments.of("admin@example.com", "admin", HttpStatus.OK),
            Arguments.of("user@example.com", "user", HttpStatus.OK),
            Arguments.of("test@example.com", "test", HttpStatus.OK),
            Arguments.of("none@example.com", "none", HttpStatus.UNAUTHORIZED)
        )

        @JvmStatic
        fun dataOfRegister(): Stream<Arguments> {
            val book = Book(789, "???????????????", "???????????????", LocalDate.of(2010, 12, 3))
            val expectedRegisteredBook = AdminBookResponse(book.id, book.title, book.author, book.releaseDate)
            val expectedBookDetail = GetBookDetailResponse(BookWithRental(book, null))
            return Stream.of(
                Arguments.of(
                    "admin@example.com",
                    "admin",
                    book,
                    HttpStatus.CREATED,
                    expectedRegisteredBook,
                    HttpStatus.OK,
                    expectedBookDetail
                ),
                Arguments.of(
                    "user@example.com",
                    "user",
                    book,
                    HttpStatus.FORBIDDEN,
                    null,
                    HttpStatus.BAD_REQUEST,
                    null
                )
            )
        }
    }

    @ParameterizedTest(name = "?????????????????????: User => {0}, Status => {2}")
    @MethodSource("users")
    fun `login when user is exist then login`(user: String, pass: String, expectedStatus: HttpStatus) {

        // Given
        // When
        val response = restTemplate.login(user, pass)

        // Then
        assertThat(response.statusCode).isEqualTo(expectedStatus)
    }

    @Test
    @DisplayName("??????????????????????????????")
    fun `bookList when list is exist then return them`() {

        // Given
        val bookInfo1 = BookInfo(100, "Kotlin??????", "??????????????????", false)
        val bookInfo2 = BookInfo(200, "Java??????", "???????????????", true)
        val bookInfo3 = BookInfo(300, "Spring??????", "?????????????????????", true)
        val bookInfo4 = BookInfo(400, "Kotlin??????", "??????????????????", false)
        val bookInfoNone = BookInfo(9999, "???????????????", "???????????????", false)

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
        restTemplate.login(user, pass)

        // When
        val response = restTemplate.getForEntity("$baseUri/book/list", String::class.java)

        // Then
        val result = jsonConverter.toObject(response.body, GetBookListResponse::class.java)
        val expected = GetBookListResponse(listOf(bookInfo1, bookInfo2, bookInfo3, bookInfo4))
        SoftAssertions().apply {
            assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
            assertThat(result?.bookList).containsExactlyInAnyOrderElementsOf(expected.bookList)
            assertThat(result?.bookList).`as`("?????????????????????????????????????????????????????????").doesNotContain(bookInfoNone)
        }.assertAll()
    }

    @ParameterizedTest(name = "?????????????????????: user => {0}, registeredStatus => {3}, getStatus => {5}")
    @MethodSource("dataOfRegister")
    fun `when book is register then get this`(
        user: String,
        pass: String,
        book: Book,
        postStatus: HttpStatus,
        expectedRegisteredBook: AdminBookResponse?,
        getStatus: HttpStatus,
        expectedBookDetail: GetBookDetailResponse?
    ) {
        // Given
        restTemplate.login(user, pass)

        // When
        val httpHeaders = HttpHeaders().apply { contentType = MediaType.APPLICATION_JSON }
        val jsonObject = JSONObject().apply {
            put("id", book.id)
            put("title", book.title)
            put("author", book.author)
            put("release_date", book.releaseDate.format(DateTimeFormatter.ISO_DATE))
        }

        val postRequest = HttpEntity<String>(jsonObject.toString(), httpHeaders)
        val postResponse = restTemplate.postForEntity("$baseUri/admin/book/register", postRequest, String::class.java)
        val registeredBook = jsonConverter.toObject(postResponse.body, AdminBookResponse::class.java)

        // Then
        val response = restTemplate.getForEntity("$baseUri/book/detail/${book.id}", String::class.java)
        val result = jsonConverter.toObject(response.body, GetBookDetailResponse::class.java)

        SoftAssertions().apply {
            assertThat(postResponse.statusCode).isEqualTo(postStatus)
            assertThat(registeredBook).isEqualTo(expectedRegisteredBook)
            assertThat(response.statusCode).isEqualTo(getStatus)
            assertThat(result).isEqualTo(expectedBookDetail)
        }.assertAll()
    }

    fun TestRestTemplate.login(user: String, pass: String): ResponseEntity<String> {

        val loginForm = LinkedMultiValueMap<String, String>().apply {
            add("email", user)
            add("pass", pass)
        }

        val request = RequestEntity
            .post(URI.create("$baseUri/login"))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.TEXT_HTML)
            .body(loginForm)

        return restTemplate.exchange(request, String::class.java)
    }
}
