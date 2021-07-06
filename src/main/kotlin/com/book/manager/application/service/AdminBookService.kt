package com.book.manager.application.service

import com.book.manager.domain.model.Book
import com.book.manager.domain.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class AdminBookService(private val bookRepository: BookRepository) {

    @Transactional
    fun register(book: Book): Book {
        return when (bookRepository.findWithRental(book.id)) {
            null -> bookRepository.register(book).run { book }
            else -> throw IllegalArgumentException("既に存在する書籍ID: ${book.id}")
        }
    }

    @Transactional
    fun update(bookId: Long, title: String?, author: String?, releaseDate: LocalDate?): Long {
        return when (bookRepository.findWithRental(bookId)) {
            null -> throw IllegalArgumentException("存在しない書籍ID: $bookId")
            else -> bookRepository.update(bookId, title, author, releaseDate).run { bookId }
        }
    }

    @Transactional
    fun delete(bookId: Long): Long {
        return when (bookRepository.findWithRental(bookId)) {
            null -> throw IllegalArgumentException("存在しない書籍ID: $bookId")
            else -> bookRepository.delete(bookId).run { bookId }
        }
    }
}