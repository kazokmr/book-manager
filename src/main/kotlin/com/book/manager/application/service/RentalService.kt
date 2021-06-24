package com.book.manager.application.service

import com.book.manager.application.service.result.Result
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
    fun startRental(bookId: Long, accountId: Long): Result {
        accountRepository.findById(accountId)
            ?: return Result.Failure("該当するユーザーが存在しません accountId: $accountId")
        val book = bookRepository.findWithRental(bookId)
            ?: return Result.Failure("該当する書籍が存在しません bookId: $bookId")

        return when (book.isRental) {
            true -> Result.Failure("貸出中の書籍です bookId: $bookId")
            false -> {
                val rentalDateTime = LocalDateTime.now()
                val returnDeadLine = rentalDateTime.plusDays(RENTAL_TERM_DAYS)
                val rental = Rental(bookId, accountId, rentalDateTime, returnDeadLine)
                rentalRepository.startRental(rental)
                Result.Success("Book $bookId has been renting by $accountId")
            }
        }
    }

    @Transactional
    fun endRental(bookId: Long, accountId: Long): Result {
        accountRepository.findById(accountId)
            ?: return Result.Failure("該当するユーザーが存在しません accountId: $accountId")
        val book = bookRepository.findWithRental(bookId)
            ?: return Result.Failure("該当する書籍が存在しません bookId: $bookId")

        if (!book.isRental) return Result.Failure("未貸出の書籍です bookId: $bookId")
        if (book.rental?.accountId != accountId) return Result.Failure("他のユーザーが貸出中の書籍です bookId: $bookId")

        rentalRepository.endRental(bookId)
        return Result.Success("Book $bookId is returning from $accountId")
    }
}