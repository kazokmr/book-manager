package com.book.manager.domain.repository

import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental

interface BookRepository {
    fun findAllWithRental(): List<BookWithRental>
    fun findWithRental(id: Long): BookWithRental?
    fun register(book: Book)
}