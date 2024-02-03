package com.book.manager

import com.book.manager.config.CustomExchangeFilterFunction
import com.book.manager.config.CustomJsonConverter
import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import com.book.manager.infrastructure.database.testcontainers.TestContainerDataRegistry
import com.book.manager.presentation.form.AdminBookResponse
import com.book.manager.presentation.form.BookInfo
import com.book.manager.presentation.form.GetBookDetailResponse
import com.book.manager.presentation.form.GetBookListResponse
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.boot.testcontainers.context.ImportTestcontainers
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.json.Jackson2JsonDecoder
import org.springframework.test.context.jdbc.Sql
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.reactive.function.BodyInserters
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.stream.Stream

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ImportTestcontainers(value = [TestContainerDataRegistry::class])
@Sql("Account.sql")
@Sql(value = ["clear.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
internal class BookManagerIntegrationTests {

    @LocalServerPort
    private var port: Int = 0

    private lateinit var webClient: WebTestClient

    private val jsonConverter: CustomJsonConverter = CustomJsonConverter()

    @BeforeEach
    internal fun setUp() {
        webClient = WebTestClient
            .bindToServer()
            .baseUrl("http://localhost:$port")
            .filter(CustomExchangeFilterFunction()) // Request/ResponseのカスタムFilter
            .codecs {
                it.defaultCodecs().jackson2JsonDecoder(Jackson2JsonDecoder(jsonConverter.objectMapper()))
            }
            .build()
        webClient.get().uri("/csrf_token").exchange()
    }

    companion object {
        @JvmStatic
        fun users(): Stream<Arguments> = Stream.of(
            Arguments.of("admin@example.com", "password", HttpStatus.OK),
            Arguments.of("user@example.com", "password", HttpStatus.OK),
            Arguments.of("test@example.com", "password", HttpStatus.OK),
            Arguments.of("none@example.com", "password", HttpStatus.UNAUTHORIZED)
        )

        @JvmStatic
        fun dataOfRegister(): Stream<Arguments> {
            val book = Book(789, "統合テスト", "テスト二郎", LocalDate.of(2010, 12, 3))
            val expectedRegisteredBook = AdminBookResponse(book.id, book.title, book.author, book.releaseDate)
            val expectedBookDetail = GetBookDetailResponse(BookWithRental(book, null))
            return Stream.of(
                Arguments.of(
                    "admin@example.com",
                    "password",
                    book,
                    HttpStatus.CREATED,
                    expectedRegisteredBook,
                    HttpStatus.OK,
                    expectedBookDetail
                ),
                Arguments.of(
                    "user@example.com",
                    "password",
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
    @Sql(value = ["Account.sql", "BookWithRental.sql"])
    @Sql(value = ["clear.sql"], executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    fun `bookList when list is exist then return them`() {

        // Given
        webClient.login("admin@example.com", "password")

        // When
        val response = webClient
            .get()
            .uri("/book/list")
            .exchange()
            .expectBody<GetBookListResponse>()
            .returnResult()

        // Then
        val expected = GetBookListResponse(
            listOf(
                BookInfo(100, "Kotlin入門", "ことりん太郎", false),
                BookInfo(200, "Java入門", "じゃば太郎", true),
                BookInfo(300, "Spring入門", "すぷりんぐ太郎", true),
                BookInfo(400, "Kotlin実践", "ことりん太郎", false),
            )
        )
        SoftAssertions().apply {
            assertThat(response.status).isEqualTo(HttpStatus.OK)
            assertThat(response.responseBody?.bookList).containsExactlyInAnyOrderElementsOf(expected.bookList)
            assertThat(response.responseBody?.bookList).`as`("登録していない書籍は含まれていないこと")
                .doesNotContain(
                    BookInfo(9999, "未登録書籍", "アノニマス", false)
                )
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
