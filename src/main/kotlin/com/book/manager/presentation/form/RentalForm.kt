package com.book.manager.presentation.form

import com.book.manager.domain.model.Rental
import java.time.LocalDateTime
import javax.validation.constraints.Min

data class RentalStartRequest(
    @field:Min(value = 1, message = "書籍IDには1以上の数値を入れてください。")
    val bookId: Long
)

data class RentalStartResponse(
    val bookId: Long,
    val accountId: Long,
    val rentalDatetime: LocalDateTime,
    val returnDeadline: LocalDateTime
) {
    constructor(rental: Rental) : this(rental.bookId, rental.accountId, rental.rentalDatetime, rental.returnDeadline)
}