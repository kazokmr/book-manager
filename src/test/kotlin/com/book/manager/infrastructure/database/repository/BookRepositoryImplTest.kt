package com.book.manager.infrastructure.database.repository

import com.book.manager.domain.model.Book
import com.book.manager.domain.repository.BookRepository
import com.book.manager.infrastructure.database.mapper.BookMapper
import com.book.manager.infrastructure.database.mapper.BookWithRentalMapper
import com.book.manager.infrastructure.database.testcontainers.TestContainerDataRegistry
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mybatis.dynamic.sql.exception.InvalidSqlException
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.boot.testcontainers.context.ImportTestcontainers
import org.springframework.dao.DuplicateKeyException
import org.springframework.test.context.jdbc.Sql
import java.time.LocalDate
import java.time.LocalDateTime

@MybatisTest
@ImportTestcontainers(TestContainerDataRegistry::class)
@AutoConfigureTestDatabase(replace = Replace.NONE)
internal class BookRepositoryImplTest {

    @Autowired
    private lateinit var rentalMapper: BookWithRentalMapper

    @Autowired
    private lateinit var bookMapper: BookMapper

    private lateinit var bookRepository: BookRepository

    @BeforeEach
    internal fun setUp() {
        bookRepository = BookRepositoryImpl(rentalMapper, bookMapper)
    }

    @Test
    @DisplayName("書籍が複数冊あれば全て検索すること")
    @Sql("MultiBooks.sql")
    fun `findAllWithRental when there are many books then get all books`() {

        // When
        val resultList =
            bookRepository.findAllWithRental().stream().sorted { o1, o2 -> o1.book.id.compareTo(o2.book.id) }.toList()

        // Then
        SoftAssertions().apply {
            assertThat(resultList.count()).`as`("4冊検索されること").isEqualTo(4)
            assertThat(resultList[0].book.id).isEqualTo(1)
            assertThat(resultList[0].book.title).isEqualTo("Kotlin入門")
            assertThat(resultList[0].book.author).isEqualTo("コトリン")
            assertThat(resultList[0].book.releaseDate).isEqualTo(LocalDate.of(2020, 6, 29))
            assertThat(resultList[0].rental).isNull()
            assertThat(resultList[1].book.id).isEqualTo(2)
            assertThat(resultList[1].book.title).isEqualTo("Spring入門")
            assertThat(resultList[1].book.author).isEqualTo("スプリング")
            assertThat(resultList[1].book.releaseDate).isEqualTo(LocalDate.of(2020, 1, 1))
            assertThat(resultList[1].rental?.accountId).isEqualTo(528)
            assertThat(resultList[1].rental?.rentalDatetime).isEqualTo(LocalDateTime.of(2021, 6, 28, 16, 28, 0, 0))
            assertThat(resultList[1].rental?.returnDeadline).isEqualTo(LocalDateTime.of(2021, 7, 12, 0, 0, 0, 0))
            assertThat(resultList[2].book.id).isEqualTo(3)
            assertThat(resultList[2].book.title).isEqualTo("Java入門")
            assertThat(resultList[2].book.author).isEqualTo("ジャヴァ")
            assertThat(resultList[2].book.releaseDate).isEqualTo(LocalDate.of(2000, 1, 1))
            assertThat(resultList[2].rental).isNull()
            assertThat(resultList[3].book.id).isEqualTo(4)
            assertThat(resultList[3].book.title).isEqualTo("Scala入門")
            assertThat(resultList[3].book.author).isEqualTo("スカラ")
            assertThat(resultList[3].book.releaseDate).isEqualTo(LocalDate.of(2001, 1, 1))
            assertThat(resultList[3].rental).isNull()
        }.assertAll()
    }

    @Test
    @DisplayName("書籍が１冊だけならその１冊だけ検索すること")
    @Sql("SingleBook.sql")
    fun `findAllWithRental when there is a book then get one`() {

        // When
        val resultList = bookRepository.findAllWithRental()

        // Then
        assertThat(resultList.size).`as`("登録件数が１件であること").isEqualTo(1)
    }

    @Test
    @DisplayName("書籍が無ければ空のリストが検索されること")
    fun `findAllWIthRental when there is no books then get employ list`() {

        // When
        val resultList = bookRepository.findAllWithRental()

        // Then
        assertThat(resultList.count()).isEqualTo(0)
        assertThat(resultList).isEmpty()
    }

    @Test
    @DisplayName("貸出されていない登録されている書籍が検索できる")
    @Sql("MultiBooks.sql")
    fun `findWithRental when book is exist and is not rent then get one without rental`() {

        // When
        val bookWithRental = bookRepository.findWithRental(1)

        // Then
        assertThat(bookWithRental?.book).isNotNull
        assertThat(bookWithRental?.isRental).isFalse
    }

    @Test
    @DisplayName("貸出されていて登録されている書籍が検索できる")
    @Sql("MultiBooks.sql")
    fun `findWithRental when book is exist and is rent then get one with rental`() {

        // When
        val bookWithRental = bookRepository.findWithRental(2)

        // Then
        assertThat(bookWithRental?.book).isNotNull
        assertThat(bookWithRental?.isRental).isTrue
    }

    @Test
    @DisplayName("書籍を登録する")
    @Sql("MultiBooks.sql")
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
    @Sql("MultiBooks.sql")
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
    @Sql("MultiBooks.sql")
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
    @Sql("MultiBooks.sql")
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
    @Sql("MultiBooks.sql")
    fun `update when book will not be updated any properties then update any columns`() {

        // Then
        assertThrows<InvalidSqlException> {
            bookRepository.update(1L, null, null, null)
        }
    }

    @Test
    @DisplayName("存在しない書籍IDなら更新されないこと")
    @Sql("MultiBooks.sql")
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
    @Sql("MultiBooks.sql")
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
    @Sql("MultiBooks.sql")
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
