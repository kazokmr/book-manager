package com.book.manager.application.service

import com.book.manager.domain.model.Rental
import com.book.manager.domain.repository.AccountRepository
import com.book.manager.domain.repository.BookRepository
import com.book.manager.domain.repository.RentalRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private const val RENTAL_TERM_DAYS = 14L

@Service
class RentalService(
    private val accountRepository: AccountRepository,
    private val bookRepository: BookRepository,
    private val rentalRepository: RentalRepository
) {
    @Transactional
    fun startRental(bookId: Long, accountId: Long) {
        accountRepository.findById(accountId) ?: throw IllegalArgumentException("該当するユーザーが存在しません accountId:$accountId")
        val book =
            bookRepository.findWithRental(bookId) ?: throw IllegalArgumentException("該当する書籍が存在しません bookId:$bookId")

        if (book.isRental) throw IllegalArgumentException("貸出中の書籍です bookId:$bookId")

        val rentalDateTime = LocalDateTime.now()
        val returnDeadLine = rentalDateTime.plusDays(RENTAL_TERM_DAYS)
        val rental = Rental(bookId, accountId, rentalDateTime, returnDeadLine)

        rentalRepository.startRental(rental)
    }

    @Transactional
    fun endRental(bookId: Long, accountId: Long) {
        accountRepository.findById(accountId) ?: throw IllegalArgumentException("該当するユーザーが存在しません accountId:$accountId")
        val book =
            bookRepository.findWithRental(bookId) ?: throw IllegalArgumentException("該当する書籍が存在しません bookId:$bookId")

        if (!book.isRental) throw IllegalArgumentException("未貸出の書籍です bookId:$bookId")
        if (book.rental?.accountId != accountId) throw IllegalArgumentException("他のユーザーが貸出中の商品です accountId:$accountId bookId:$bookId")

        rentalRepository.endRental(bookId)
    }
}