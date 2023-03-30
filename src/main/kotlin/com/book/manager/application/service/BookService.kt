package com.book.manager.application.service

import com.book.manager.domain.model.BookWithRental
import com.book.manager.domain.repository.BookRepository
import io.micrometer.observation.annotation.Observed
import org.springframework.stereotype.Service

@Observed
@Service
class BookService(private val bookRepository: BookRepository) {
    fun getList(): List<BookWithRental> = bookRepository.findAllWithRental()

    fun getDetail(bookId: Long): BookWithRental =
        bookRepository.findWithRental(bookId) ?: throw IllegalArgumentException("存在しない書籍ID: $bookId")
}