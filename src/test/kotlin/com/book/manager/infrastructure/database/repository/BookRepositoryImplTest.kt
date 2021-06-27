package com.book.manager.infrastructure.database.repository

import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import com.book.manager.domain.model.Rental
import com.book.manager.domain.repository.BookRepository
import com.book.manager.infrastructure.database.mapper.BookDynamicSqlSupport
import com.book.manager.infrastructure.database.mapper.BookMapper
import com.book.manager.infrastructure.database.mapper.RentalMapper
import com.book.manager.infrastructure.database.mapper.delete
import com.book.manager.infrastructure.database.mapper.insert
import com.book.manager.infrastructure.database.mapper.selectByPrimaryKey
import com.book.manager.infrastructure.database.record.BookRecord
import com.book.manager.infrastructure.database.record.RentalRecord
import com.book.manager.infrastructure.database.testcontainers.TestContainerPostgres
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mybatis.dynamic.sql.util.kotlin.spring.deleteFrom
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.context.annotation.Import
import org.springframework.dao.DuplicateKeyException
import org.springframework.jdbc.BadSqlGrammarException
import java.time.LocalDate
import java.time.LocalDateTime

@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(BookRepositoryImpl::class)
internal class BookRepositoryImplTest : TestContainerPostgres() {

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var bookMapper: BookMapper

    @Autowired
    private lateinit var rentalMapper: RentalMapper

    @BeforeEach
    fun setUp() {
    }

    @Test
    @DisplayName("書籍が複数冊あれば全て検索すること")
    fun `findAllWithRental when there are many books then get all books`() {

        // Given
        val book1 = Book(1, "Kotlin入門", "コトリン", LocalDate.now())
        val book2 = Book(2, "Spring入門", "スプリング", LocalDate.now())

        bookMapper.insert(BookRecord(book1.id, book1.title, book1.author, book1.releaseDate))
        bookMapper.insert(BookRecord(book2.id, book2.title, book2.author, book2.releaseDate))

        val expected = listOf(BookWithRental(book1, null), BookWithRental(book2, null))

        // When
        val resultList = bookRepository.findAllWithRental()

        // Then
        assertThat(resultList.count()).isEqualTo(2)
        assertThat(resultList).containsExactlyInAnyOrderElementsOf(expected)
    }

    @Test
    @DisplayName("書籍が１冊だけならその１冊だけ検索すること")
    fun `findAllWithRental when there is a book then get one`() {

        // Given
        val book1 = Book(1, "Kotlin入門", "コトリン", LocalDate.now())
        bookMapper.insert(BookRecord(book1.id, book1.title, book1.author, book1.releaseDate))

        // When
        val resultList = bookRepository.findAllWithRental()

        // Then
        assertThat(resultList.count()).isEqualTo(1)
        assertThat(resultList).containsOnlyOnce(BookWithRental(book1, null))
    }

    @Test
    @DisplayName("書籍が無ければ空のリストが検索されること")
    fun `findAllWIthRental when there is no books then get employ list`() {

        // Given
        bookMapper.delete(deleteFrom(BookDynamicSqlSupport.Book) { allRows() })

        // When
        val resultList = bookRepository.findAllWithRental()

        // Then
        assertThat(resultList.count()).isEqualTo(0)
        assertThat(resultList).isEmpty()
    }

    @Test
    @DisplayName("貸出されていない登録されている書籍が検索できる")
    fun `findWithRental when book is exist and is not rent then get one without rental`() {

        // Given
        val book = Book(1, "Kotlin入門", "コトリン", LocalDate.now())
        val bookRecord = BookRecord(book.id, book.title, book.author, book.releaseDate)
        bookMapper.insert(bookRecord)

        // When
        val bookWithRental = bookRepository.findWithRental(book.id)

        // Then
        assertThat(bookWithRental?.book).isEqualTo(book)
        assertThat(bookWithRental?.isRental).isFalse
    }


    @Test
    @DisplayName("貸出されていて登録されている書籍が検索できる")
    fun `findWithRental when book is exist and is rent then get one with rental`() {

        // Given
        val book = Book(1, "Kotlin入門", "コトリン", LocalDate.now())
        val bookRecord = BookRecord(book.id, book.title, book.author, book.releaseDate)
        bookMapper.insert(bookRecord)

        val rental = Rental(book.id, 100, LocalDateTime.now(), LocalDateTime.now().plusDays(14))
        val rentalRecord = RentalRecord(rental.bookId, rental.accountId, rental.rentalDatetime, rental.returnDeadline)
        rentalMapper.insert(rentalRecord)

        // When
        val bookWithRental = bookRepository.findWithRental(book.id)

        // Then
        assertThat(bookWithRental?.book).isEqualTo(book)
        assertThat(bookWithRental?.isRental).isTrue
    }

    @Test
    @DisplayName("書籍を登録する")
    fun `register when a book is not registered yet then register it`() {

        // Given
        bookMapper.delete { allRows() }
        val book = Book(1, "Kotlin入門", "コトリン", LocalDate.now())

        // When
        val count = bookRepository.register(book)

        // Then
        assertThat(count).isEqualTo(1)
        val result = bookMapper.selectByPrimaryKey(book.id)
        SoftAssertions().apply {
            assertThat(result?.id).isEqualTo(book.id)
            assertThat(result?.title).isEqualTo(book.title)
            assertThat(result?.author).isEqualTo(book.author)
            assertThat(result?.releaseDate).isEqualTo(book.releaseDate)
        }.assertAll()
    }

    @Test
    @DisplayName("既にIDが登録済みの書籍は登録できない")
    fun `register when a book has already registered then will not register it`() {

        // Given
        bookMapper.delete { allRows() }
        val registeredBook = Book(1, "Kotlin入門", "コトリン", LocalDate.now())
        val registeredRecord =
            BookRecord(registeredBook.id, registeredBook.title, registeredBook.author, registeredBook.releaseDate)
        bookMapper.insert(registeredRecord)

        // When
        val book = Book(1, "Kotlin再入門", "コトリン", LocalDate.now())

        // Then
        assertThrows<DuplicateKeyException> {
            bookRepository.register(book)
        }
    }

    @Test
    @DisplayName("既存IDを持つ書籍情報のプロパティを更新する")
    fun `update when book id has already been registered then update its properties`() {

        // Given
        bookMapper.delete { allRows() }
        val book = Book(1, "Kotlin入門", "コトリン", LocalDate.now())
        val bookRecord = BookRecord(book.id, book.title, book.author, book.releaseDate)
        bookMapper.insert(bookRecord)

        // When
        val updatedTitle = "Spring入門"
        val updatedAuthor = "スプリング"
        val updatedReleaseDate = LocalDate.now().plusDays(60)
        val count = bookRepository.update(book.id, updatedTitle, updatedAuthor, updatedReleaseDate)

        // Then
        assertThat(count).isEqualTo(1)

        val result = bookMapper.selectByPrimaryKey(book.id)
        SoftAssertions().apply {
            assertThat(result?.id).isEqualTo(book.id)
            assertThat(result?.title).isEqualTo(updatedTitle)
            assertThat(result?.author).isEqualTo(updatedAuthor)
            assertThat(result?.releaseDate).isEqualTo(updatedReleaseDate)
        }.assertAll()
    }

    @Test
    @DisplayName("変更したプロパティだけが更新されること")
    fun `update when book will be updated some properties then update only their updated properties`() {

        // Given
        bookMapper.delete { allRows() }
        val book = Book(1, "Kotlin入門", "コトリン", LocalDate.now())
        val bookRecord = BookRecord(book.id, book.title, book.author, book.releaseDate)
        bookMapper.insert(bookRecord)

        // When
        val updatedTitle = "Spring入門"
        val count = bookRepository.update(book.id, updatedTitle, null, null)

        // Then
        assertThat(count).isEqualTo(1)

        val result = bookMapper.selectByPrimaryKey(book.id)
        SoftAssertions().apply {
            assertThat(result?.id).isEqualTo(book.id)
            assertThat(result?.title).isEqualTo(updatedTitle)
            assertThat(result?.author).isEqualTo(book.author)
            assertThat(result?.releaseDate).isEqualTo(book.releaseDate)
        }.assertAll()
    }

    @Test
    @DisplayName("プロパティを何も変更しなければ更新されないこと")
    fun `update when book will not be updated any properties then update any columns`() {

        // Given
        bookMapper.delete { allRows() }
        val book = Book(1, "Kotlin入門", "コトリン", LocalDate.now())
        val bookRecord = BookRecord(book.id, book.title, book.author, book.releaseDate)
        bookMapper.insert(bookRecord)

        // Then
        assertThrows<BadSqlGrammarException> {
            bookRepository.update(book.id, null, null, null)
        }
    }

    @Test
    @DisplayName("存在しない書籍IDなら更新されないこと")
    fun `update when book id is not exist then book will not be updated`() {

        // Given
        bookMapper.delete { allRows() }

        // When
        val updatedTitle = "Spring入門"
        val updatedAuthor = "スプリング"
        val updatedReleaseDate = LocalDate.now().plusDays(60)
        val count = bookRepository.update(1, updatedTitle, updatedAuthor, updatedReleaseDate)

        // Then
        assertThat(count).isEqualTo(0)
    }

    @Test
    @DisplayName("書籍を削除する")
    fun `delete when book is exist then delete it`() {

        // Given
        bookMapper.delete { allRows() }
        val book = Book(1, "Kotlin入門", "コトリン", LocalDate.now())
        val bookRecord = BookRecord(book.id, book.title, book.author, book.releaseDate)
        bookMapper.insert(bookRecord)

        // When
        val count = bookRepository.delete(book.id)

        // Then
        assertThat(count).isEqualTo(1)
        val result = bookMapper.selectByPrimaryKey(book.id)
        assertThat(result).isNull()
    }

    @Test
    @DisplayName("登録されていない書籍を削除しても削除できないこと")
    fun `delete when book will be deleted without registration then number of delete is zero`() {

        // Given
        bookMapper.delete { allRows() }

        // When
        val count = bookRepository.delete(1)

        // Then
        assertThat(count).isEqualTo(0)
    }
}