package com.book.manager.infrastructure.database.repository

import com.book.manager.domain.model.Rental
import com.book.manager.domain.repository.RentalRepository
import com.book.manager.infrastructure.database.mapper.RentalMapper
import com.book.manager.infrastructure.database.mapper.deleteByPrimaryKey
import com.book.manager.infrastructure.database.mapper.insert
import io.micrometer.observation.annotation.Observed
import org.springframework.stereotype.Repository
import com.book.manager.infrastructure.database.record.Rental as RecordRental

@Observed
@Repository
class RentalRepositoryImpl(private val rentalMapper: RentalMapper) : RentalRepository {

    override fun startRental(rental: Rental): Int {
        return rentalMapper.insert(toRecord(rental))
    }

    override fun endRental(bookId: Long): Int {
        return rentalMapper.deleteByPrimaryKey(bookId)
    }

    private fun toRecord(rental: Rental): RecordRental {
        return RecordRental(rental.bookId, rental.accountId, rental.rentalDatetime, rental.returnDeadline)
    }
}