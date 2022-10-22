package com.book.manager.infrastructure.database.repository

import com.book.manager.domain.model.Book
import com.book.manager.domain.repository.BookRepository
import com.book.manager.infrastructure.database.dbunit.DataSourceConfig
import com.book.manager.infrastructure.database.testcontainers.TestContainerDataRegistry
import com.github.springtestdbunit.DbUnitTestExecutionListener
import com.github.springtestdbunit.annotation.DatabaseSetup
import com.github.springtestdbunit.annotation.ExpectedDatabase
import com.github.springtestdbunit.assertion.DatabaseAssertionMode.NON_STRICT_UNORDERED
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mybatis.dynamic.sql.exception.InvalidSqlException
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.context.annotation.Import
import org.springframework.dao.DuplicateKeyException
import org.springframework.test.context.TestExecutionListeners
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener
import java.time.LocalDate

@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(value = [BookRepositoryImpl::class, DataSourceConfig::class])
@TestExecutionListeners(listeners = [DependencyInjectionTestExecutionListener::class, DbUnitTestExecutionListener::class])
internal class BookRepositoryImplTest : TestContainerDataRegistry() {

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Test
    @DisplayName("書籍が複数冊あれば全て検索すること")
    @DatabaseSetup("/test-data/book/initMultiRecord.xml")
    @ExpectedDatabase("/test-data/book/expectedMultiRecord.xml", assertionMode = NON_STRICT_UNORDERED)
    fun `findAllWithRental when there are many books then get all books`() {

        // When
        val resultList = bookRepository.findAllWithRental()

        // Then
        assertThat(resultList.count()).`as`("4冊検索されること").isEqualTo(4)
    }

    @Test
    @DisplayName("書籍が１冊だけならその１冊だけ検索すること")
    @DatabaseSetup("/test-data/book/initSingleRecord.xml")
    @ExpectedDatabase("/test-data/book/expectedSingleRecord.xml", assertionMode = NON_STRICT_UNORDERED)
    fun `findAllWithRental when there is a book then get one`() {

        // When
        val resultList = bookRepository.findAllWithRental()

        // Then
        assertThat(resultList.size).`as`("登録件数が１件であること").isEqualTo(1)
    }

    @Test
    @DisplayName("書籍が無ければ空のリストが検索されること")
    @DatabaseSetup("/test-data/book/initNoRecord.xml")
    fun `findAllWIthRental when there is no books then get employ list`() {

        // When
        val resultList = bookRepository.findAllWithRental()

        // Then
        assertThat(resultList.count()).isEqualTo(0)
        assertThat(resultList).isEmpty()
    }

    @Test
    @DisplayName("貸出されていない登録されている書籍が検索できる")
    @DatabaseSetup("/test-data/book/initMultiRecord.xml")
    @ExpectedDatabase("/test-data/book/expectedMultiRecord.xml", assertionMode = NON_STRICT_UNORDERED)
    fun `findWithRental when book is exist and is not rent then get one without rental`() {

        // When
        val bookWithRental = bookRepository.findWithRental(1)

        // Then
        assertThat(bookWithRental?.book).isNotNull
        assertThat(bookWithRental?.isRental).isFalse
    }

    @Test
    @DisplayName("貸出されていて登録されている書籍が検索できる")
    @DatabaseSetup("/test-data/book/initMultiRecord.xml")
    @ExpectedDatabase("/test-data/book/expectedMultiRecord.xml", assertionMode = NON_STRICT_UNORDERED)
    fun `findWithRental when book is exist and is rent then get one with rental`() {

        // When
        val bookWithRental = bookRepository.findWithRental(2)

        // Then
        assertThat(bookWithRental?.book).isNotNull
        assertThat(bookWithRental?.isRental).isTrue
    }

    @Test
    @DisplayName("書籍を登録する")
    @DatabaseSetup("/test-data/book/initMultiRecord.xml")
    fun `register when a book is not registered yet then register it`() {

        // Given
        val book = Book(100, "hoge入門", "hogehoge", LocalDate.now())

        // When
        val registeredCount = bookRepository.register(book)
        val resultCount = bookRepository.findAllWithRental().count()
        val resultBook = bookRepository.findWithRental(book.id)?.book

        // Then
        assertThat(registeredCount).isEqualTo(1)
        assertThat(resultCount).isEqualTo(5)
        assertThat(resultBook).isNotNull
        SoftAssertions().apply {
            assertThat(resultBook?.id).isEqualTo(book.id)
            assertThat(resultBook?.title).isEqualTo(book.title)
            assertThat(resultBook?.author).isEqualTo(book.author)
            assertThat(resultBook?.releaseDate).isEqualTo(book.releaseDate)
        }.assertAll()
    }

    @Test
    @DisplayName("既にIDが登録済みの書籍は登録できない")
    @DatabaseSetup("/test-data/book/initMultiRecord.xml")
    fun `register when a book has already registered then will not register it`() {

        // When
        val book = Book(1, "Kotlin再入門", "コトリン", LocalDate.now())

        // Then
        assertThrows<DuplicateKeyException> {
            bookRepository.register(book)
        }
    }

    @Test
    @DisplayName("既存IDを持つ書籍情報のプロパティを更新する")
    @DatabaseSetup("/test-data/book/initMultiRecord.xml")
    fun `update when book id has already been registered then update its properties`() {

        // Given
        val bookId = 1L
        val updatedTitle = "Spring入門"
        val updatedAuthor = "スプリング"
        val updatedReleaseDate = LocalDate.now().plusDays(60)

        // When
        val updatedCount = bookRepository.update(bookId, updatedTitle, updatedAuthor, updatedReleaseDate)
        val resultCount = bookRepository.findAllWithRental().count()
        val resultBook = bookRepository.findWithRental(bookId)?.book

        // Then
        assertThat(updatedCount).isEqualTo(1)
        assertThat(resultCount).isEqualTo(4)
        SoftAssertions().apply {
            assertThat(resultBook?.id).isEqualTo(bookId)
            assertThat(resultBook?.title).isEqualTo(updatedTitle)
            assertThat(resultBook?.author).isEqualTo(updatedAuthor)
            assertThat(resultBook?.releaseDate).isEqualTo(updatedReleaseDate)
        }.assertAll()
    }

    @Test
    @DisplayName("変更したプロパティだけが更新されること")
    @DatabaseSetup("/test-data/book/initMultiRecord.xml")
    fun `update when book will be updated some properties then update only their updated properties`() {

        // Given
        val bookId = 3L
        val updatedTitle = "Spring入門"
        val author = "ジャヴァ"
        val releaseDate = LocalDate.parse("2000-01-01")

        // When
        val updatedCount = bookRepository.update(bookId, updatedTitle, null, null)
        val resultCount = bookRepository.findAllWithRental().count()
        val resultBook = bookRepository.findWithRental(bookId)?.book

        // Then
        assertThat(updatedCount).isEqualTo(1)
        assertThat(resultCount).isEqualTo(4)
        SoftAssertions().apply {
            assertThat(resultBook?.id).isEqualTo(bookId)
            assertThat(resultBook?.title).isEqualTo(updatedTitle)
            assertThat(resultBook?.author).isEqualTo(author)
            assertThat(resultBook?.releaseDate).isEqualTo(releaseDate)
        }.assertAll()
    }

    @Test
    @DisplayName("プロパティを何も変更しなければ更新されないこと")
    @DatabaseSetup("/test-data/book/initMultiRecord.xml")
    fun `update when book will not be updated any properties then update any columns`() {

        // Then
        assertThrows<InvalidSqlException> {
            bookRepository.update(1L, null, null, null)
        }
    }

    @Test
    @DisplayName("存在しない書籍IDなら更新されないこと")
    @DatabaseSetup("/test-data/book/initMultiRecord.xml")
    fun `update when book id is not exist then book will not be updated`() {

        // Given
        val bookId = 5L
        val updatedTitle = "Groovy入門"
        val updatedAuthor = "グルービー"
        val updatedReleaseDate = LocalDate.now().plusDays(60)

        //When
        val updatedCount = bookRepository.update(bookId, updatedTitle, updatedAuthor, updatedReleaseDate)
        val resultCount = bookRepository.findAllWithRental().count()

        // Then
        assertThat(updatedCount).isEqualTo(0)
        assertThat(resultCount).isEqualTo(4)
    }

    @Test
    @DisplayName("書籍を削除する")
    @DatabaseSetup("/test-data/book/initMultiRecord.xml")
    fun `delete when book is exist then delete it`() {

        // Given
        val bookId = 1L

        // When
        val deletedCount = bookRepository.delete(bookId)
        val resultCount = bookRepository.findAllWithRental().count()
        val resultBookWithRental = bookRepository.findWithRental(bookId)

        // Then
        assertThat(deletedCount).isEqualTo(1)
        assertThat(resultCount).isEqualTo(3)
        assertThat(resultBookWithRental).isNull()
    }

    @Test
    @DisplayName("登録されていない書籍を削除しても削除できないこと")
    @DatabaseSetup("/test-data/book/initMultiRecord.xml")
    fun `delete when book will be deleted without registration then number of delete is zero`() {

        // Given
        val bookId = 528L

        // When
        val deletedCount = bookRepository.delete(bookId)
        val resultCount = bookRepository.findAllWithRental().count()
        val resultBookWithRental = bookRepository.findWithRental(bookId)

        // Then
        assertThat(deletedCount).isEqualTo(0)
        assertThat(resultCount).isEqualTo(4)
        assertThat(resultBookWithRental).isNull()
    }
}