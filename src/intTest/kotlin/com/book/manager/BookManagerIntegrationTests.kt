package com.book.manager

import com.book.manager.config.CustomExchangeFilterFunction
import com.book.manager.config.CustomJsonConverter
import com.book.manager.config.CustomTestMapper
import com.book.manager.config.IntegrationTestConfiguration
import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import com.book.manager.domain.model.Rental
import com.book.manager.presentation.form.AdminBookResponse
import com.book.manager.presentation.form.BookInfo
import com.book.manager.presentation.form.GetBookDetailResponse
import com.book.manager.presentation.form.GetBookListResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
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
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.stream.Stream

@SpringBootTest(webEnvironment = RANDOM_PORT)
@Import(IntegrationTestConfiguration::class)
internal class BookManagerIntegrationTests {

    @Autowired
    private lateinit var exchangeFilter: CustomExchangeFilterFunction

    @Autowired
    private lateinit var jsonConverter: CustomJsonConverter

    @Autowired
    private lateinit var testMapper: CustomTestMapper

    @LocalServerPort
    private var port: Int = 0

    private lateinit var webClient: WebTestClient

    @BeforeEach
    internal fun setUp() {
        testMapper.initDefaultAccounts()
        webClient = WebTestClient
            .bindToServer()
            .baseUrl("http://localhost:$port")
            .filter(exchangeFilter)
            .codecs {
                it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(jsonConverter.objectMapper()))
            }
            .build()
        webClient.get().uri("/csrf_token").exchange()
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
            val book = Book(789, "統合テスト", "テスト二郎", LocalDate.of(2010, 12, 3))
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

    @ParameterizedTest(name = "ログインテスト: User => {0}, Status => {2}")
    @MethodSource("users")
    @DisplayName("ログイン認証テスト")
    fun `login when user is exist then login`(user: String, pass: String, expectedStatus: HttpStatus) {

        // Given
        // When
        val response = webClient.login(user, pass).expectBody<String>().returnResult()

        // Then
        assertThat(response.status).isEqualTo(expectedStatus)
    }

    @Test
    @DisplayName("書籍リストを取得する")
    fun `bookList when list is exist then return them`() {

        // Given
        val bookInfo1 = BookInfo(100, "Kotlin入門", "ことりん太郎", false)
        val bookInfo2 = BookInfo(200, "Java入門", "じゃば太郎", true)
        val bookInfo3 = BookInfo(300, "Spring入門", "すぷりんぐ太郎", true)
        val bookInfo4 = BookInfo(400, "Kotlin実践", "ことりん太郎", false)
        val bookInfoNone = BookInfo(9999, "未登録書籍", "アノニマス", false)

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
        webClient.login(user, pass)

        // When
        val response = webClient
            .get()
            .uri("/book/list")
            .exchange()
            .expectBody<GetBookListResponse>()
            .returnResult()

        // Then
        val expected = GetBookListResponse(listOf(bookInfo1, bookInfo2, bookInfo3, bookInfo4))
        SoftAssertions().apply {
            assertThat(response.status).isEqualTo(HttpStatus.OK)
            assertThat(response.responseBody?.bookList).containsExactlyInAnyOrderElementsOf(expected.bookList)
            assertThat(response.responseBody?.bookList).`as`("登録していない書籍は含まれていないこと").doesNotContain(bookInfoNone)
        }.assertAll()
    }

    @ParameterizedTest(name = "書籍を登録する: user => {0}, registeredStatus => {3}, getStatus => {5}")
    @MethodSource("dataOfRegister")
    @DisplayName("書籍を登録する")
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
        webClient.login(user, pass)

        // When
        val objectMapper = jsonConverter.objectMapper()
        val jsonObject = objectMapper.createObjectNode().apply {
            put("id", book.id)
            put("title", book.title)
            put("author", book.author)
            put("release_date", book.releaseDate.format(DateTimeFormatter.ISO_DATE))
        }.let { objectMapper.writeValueAsString(it) }

        val postResponse = webClient
            .post()
            .uri("/admin/book/register")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(jsonObject)
            .exchange()
            .expectBody<AdminBookResponse>()
            .returnResult()


        // Then
        val getResponse = webClient
            .get()
            .uri("/book/detail/${book.id}")
            .exchange()
            .expectBody<String>()
            .returnResult()

        // 登録できないケースではGetBookDetailResponseのプロパティがNon-Nullのため `expectBody<GetBookDetailResponse>()` で変換すると例外が発生する、
        // このため専用の変換処理を使ってNullを返すようにしている
        val result = jsonConverter.toObject(getResponse.responseBody, GetBookDetailResponse::class.java)

        SoftAssertions().apply {
            assertThat(postResponse.status).isEqualTo(postStatus)
            assertThat(postResponse.responseBody).isEqualTo(expectedRegisteredBook)
            assertThat(getResponse.status).isEqualTo(getStatus)
            assertThat(result).isEqualTo(expectedBookDetail)
        }.assertAll()
    }

    fun WebTestClient.login(user: String, pass: String): WebTestClient.ResponseSpec {

        val loginForm = LinkedMultiValueMap<String, String>().apply {
            add("email", user)
            add("pass", pass)
        }

        return webClient
            .post()
            .uri("/login")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.TEXT_HTML)
            .body(BodyInserters.fromFormData(loginForm))
            .exchange()
    }
}
