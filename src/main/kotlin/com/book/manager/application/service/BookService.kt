package com.book.manager.application.service

import com.book.manager.application.service.result.Result
import com.book.manager.domain.model.BookWithRental
import com.book.manager.domain.repository.BookRepository
import org.springframework.stereotype.Service

@Service
class BookService(private val bookRepository: BookRepository) {
    fun getList(): List<BookWithRental> {
        return bookRepository.findAllWithRental()
    }

    fun getDetail(bookId: Long): Result {
        return bookRepository.findWithRental(bookId)?.let { Result.Success(it) }
            ?: Result.Failure("存在しない書籍ID: $bookId")
    }
}