package com.book.manager.domain.repository

import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import java.time.LocalDate

interface BookRepository {
    fun findAllWithRental(): List<BookWithRental>
    fun findWithRental(id: Long): BookWithRental?
    fun register(book: Book): Int
    fun update(id: Long, title: String?, author: String?, releaseDate: LocalDate?): Int
    fun delete(id: Long): Int
}