package com.book.manager.presentation.aop

import com.book.manager.application.service.BookService
import com.book.manager.application.service.mockuser.WithCustomMockUser
import com.book.manager.application.service.security.BookManagerUserDetails
import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import com.book.manager.presentation.controller.BookController
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Appender
import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.LoggerContext
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus.BAD_REQUEST
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpSession
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Controllerクラスに対するログ出力なのでWebMvcTestとMockUserを付ける
@WebMvcTest(controllers = [BookController::class])
@WithCustomMockUser
internal class LoggingAdviceTest {

    // Controllerテストに必要なMockBean
    @MockBean
    private lateinit var bookService: BookService

    // AOP(ログ出力)対象クラス
    private lateinit var proxy: BookController

    // Session情報を取得するためのMockリクエスト
    private lateinit var request: MockHttpServletRequest

    // 検査用のログ出力先となるAppender
    private lateinit var mockAppender: Appender

    // 出力したログを保持する
    @Captor
    private lateinit var logMessageCaptor: ArgumentCaptor<LogEvent>

    @BeforeEach
    internal fun setUp() {

        // Controllerクラスの実行をAspectJProxyFactoryでフィルタリングして、追加したAdviceクラスを実行させる
        val bookController = BookController(bookService)
        val factory = AspectJProxyFactory(bookController)
        factory.addAspect(LoggingAdvice())
        proxy = factory.getProxy()

        // Log出力でセッション情報を出力するためのMock
        val session = MockHttpSession()
        request = MockHttpServletRequest().apply { setSession(session) }
        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))

        // AOPで出力するログを検査するためのAppenderモックを設定
        mockAppender = mock()
        reset(mockAppender)
        whenever(mockAppender.name).thenReturn("MockAppender")
        whenever(mockAppender.isStarted).thenReturn(true)
        whenever(mockAppender.isStopped).thenReturn(false)

        // AppenderモックをLog出力設定に追加
        val ctx = LoggerContext.getContext(false)
        ctx.configuration.addAppender(mockAppender)
        ctx.configuration.getLoggerConfig(LogManager.ROOT_LOGGER_NAME).apply {
            level = Level.INFO
            addAppender(mockAppender, Level.INFO, null)
        }
        ctx.updateLoggers()
    }

    @AfterEach
    internal fun tearDown() {
        // Appenderモックを除去する
        val ctx = LoggerContext.getContext(false)
        ctx.configuration.loggers.values.forEach { it.removeAppender(mockAppender.name) }
        ctx.configuration.rootLogger.removeAppender(mockAppender.name)
    }

    @Test
    @DisplayName("戻り値があるメソッドのログが出力されること")
    fun `logging when there is return value then logging`() {

        // Given
        val book = Book(9999, "title", "author", LocalDate.now())
        whenever(bookService.getDetail(any() as Long)).thenReturn(BookWithRental(book, null))

        val userDetails = SecurityContextHolder.getContext().authentication.principal as BookManagerUserDetails
        val method = proxy.javaClass.declaredMethods.first { it.name == "getDetail" }

        // When
        proxy.getDetail(book.id)

        // Then
        verify(mockAppender, times(7)).append(logMessageCaptor.capture())

        // 出力されるログのメッセージを順番に文字列で取得しておく
        val logMessages = logMessageCaptor.allValues.map { it.message.formattedMessage }
        SoftAssertions().apply {
            assertThat(logMessages[0])
                .startsWith("Start Proceed:")
                .contains(method.returnType.simpleName)
                .contains(method.name)
                .endsWith("accountId=${userDetails.id}")
            assertThat(logMessages[1])
                .startsWith("Start:")
                .contains(method.returnType.simpleName)
                .contains(method.name)
                .endsWith("accountId=${userDetails.id}")
            assertThat(logMessages[2])
                .startsWith("Class: class ${BookController::class.java.canonicalName}")
            assertThat(logMessages[3])
                .isEqualTo("Session: ${request.session?.id}")
            assertThat(logMessages[4])
                .startsWith("End:")
                .contains(method.returnType.simpleName)
                .contains(method.name)
                .endsWith(
                    """
                        returnValue=${method.returnType.simpleName}(
                        id=${book.id}, 
                        title=${book.title}, 
                        author=${book.author}, 
                        releaseDate=${book.releaseDate.format(DateTimeFormatter.ISO_DATE)}, 
                        rentalInfo=null
                        )
                    """.trimIndent().replace("\n", "")
                )
            assertThat(logMessages[5])
                .startsWith("End:")
                .contains(method.returnType.simpleName)
                .contains(method.name)
                .endsWith("accountId=${userDetails.id}")
            assertThat(logMessages[6])
                .startsWith("End Proceed:")
                .contains(method.returnType.simpleName)
                .contains(method.name)
                .endsWith("accountId=${userDetails.id}")
        }.assertAll()
    }

    @Test
    @DisplayName("例外が投げられたらログレベルErrorのログを出力する")
    fun afterThrowingLog() {

        // Given
        val bookId: Long = 824
        val reason = "存在しない書籍: $bookId"
        whenever(bookService.getDetail(any() as Long)).thenThrow(IllegalArgumentException(reason))

        // When
        assertThrows<ResponseStatusException> {
            proxy.getDetail(bookId)
        }

        // Then
        verify(mockAppender, times(6)).append(logMessageCaptor.capture())
        assertThat(logMessageCaptor.allValues.count { it.level == Level.ERROR }).isEqualTo(1)

        val errorLog = logMessageCaptor.allValues.first { it.level == Level.ERROR }
        assertThat(errorLog.message.formattedMessage)
            .startsWith("Exception: ")
            .endsWith("message=${BAD_REQUEST.value()} ${BAD_REQUEST.name} \"$reason\"")
    }
}