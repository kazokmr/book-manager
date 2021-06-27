package com.book.manager.domain.repository

import com.book.manager.domain.model.Rental

interface RentalRepository {
    fun startRental(rental: Rental): Int
    fun endRental(bookId: Long): Int
}