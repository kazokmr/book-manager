package com.book.manager.infrastructure.database.repository

import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import com.book.manager.domain.model.Rental
import com.book.manager.domain.repository.BookRepository
import com.book.manager.infrastructure.database.mapper.BookMapper
import com.book.manager.infrastructure.database.mapper.BookWithRentalMapper
import com.book.manager.infrastructure.database.mapper.deleteByPrimaryKey
import com.book.manager.infrastructure.database.mapper.insert
import com.book.manager.infrastructure.database.mapper.select
import com.book.manager.infrastructure.database.mapper.selectByPrimaryKey
import com.book.manager.infrastructure.database.mapper.updateByPrimaryKeySelective
import com.book.manager.infrastructure.database.record.BookRecord
import com.book.manager.infrastructure.database.record.BookWithRentalRecord
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class BookRepositoryImpl(
    private val bookWithRentalMapper: BookWithRentalMapper,
    private val bookMapper: BookMapper
) : BookRepository {

    override fun findAllWithRental(): List<BookWithRental> = bookWithRentalMapper.select().map { toModel(it) }

    override fun findWithRental(id: Long): BookWithRental? =
        bookWithRentalMapper.selectByPrimaryKey(id)?.let { toModel(it) }

    override fun register(book: Book): Int = bookMapper.insert(toRecord(book))

    override fun update(id: Long, title: String?, author: String?, releaseDate: LocalDate?): Int =
        bookMapper.updateByPrimaryKeySelective(BookRecord(id, title, author, releaseDate))

    override fun delete(id: Long): Int = bookMapper.deleteByPrimaryKey(id)

    private fun toRecord(model: Book): BookRecord {
        return BookRecord(model.id, model.title, model.author, model.releaseDate)
    }

    private fun toModel(record: BookWithRentalRecord): BookWithRental {
        val book = Book(
            record.id!!,
            record.title!!,
            record.author!!,
            record.releaseDate!!
        )

        val rental = record.accountId?.let {
            Rental(
                record.id!!,
                record.accountId!!,
                record.rentalDateTime!!,
                record.returnDeadline!!
            )
        }
        return BookWithRental(book, rental)
    }
}