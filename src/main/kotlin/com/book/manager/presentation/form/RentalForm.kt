package com.book.manager.presentation.form

import com.book.manager.domain.model.Rental
import java.time.LocalDateTime

data class RentalStartRequest(val bookId: Long)

data class RentalStartResponse(
    val bookId: Long,
    val accountId: Long,
    val rentalDatetime: LocalDateTime,
    val returnDeadline: LocalDateTime
) {
    constructor(rental: Rental) : this(rental.bookId, rental.accountId, rental.rentalDatetime, rental.returnDeadline)
}