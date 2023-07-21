package com.book.manager.config

import com.book.manager.domain.enum.RoleType
import com.book.manager.domain.model.Account
import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import com.book.manager.domain.model.Rental
import com.book.manager.infrastructure.database.mapper.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestComponent
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import com.book.manager.infrastructure.database.record.Account as RecordAccount
import com.book.manager.infrastructure.database.record.Book as RecordBook
import com.book.manager.infrastructure.database.record.Rental as RecordRental

@TestComponent
class CustomTestMapper {

    @Autowired
    private lateinit var accountMapper: AccountMapper

    @Autowired
    private lateinit var bookMapper: BookMapper

    @Autowired
    private lateinit var rentalMapper: RentalMapper

    fun clearAllData() {
        rentalMapper.delete { allRows() }
        bookMapper.delete { allRows() }
        accountMapper.delete { allRows() }
    }

    fun createAccount(accountList: List<Account>) {
        accountList
            .map { RecordAccount(it.id, it.email, it.password.encode(), it.name, it.roleType) }
            .forEach { accountMapper.insert(it) }
    }

    fun createBook(bookList: List<Book>) {
        bookList
            .map { RecordBook(it.id, it.title, it.author, it.releaseDate) }
            .let { bookMapper.insertMultiple(it) }
    }

    fun createRental(rentalList: List<Rental>) {
        rentalList
            .map { RecordRental(it.bookId, it.accountId, it.rentalDatetime, it.rentalDatetime) }
            .let { rentalMapper.insertMultiple(it) }
    }

    fun createBookWithRental(bookWithRentalList: List<BookWithRental>) {
        createBook(bookWithRentalList.map { it.book })
        createRental(bookWithRentalList.mapNotNull { it.rental })
    }

    fun initDefaultAccounts() {
        listOf(
            Account(1, "admin@example.com", "admin", "admin", RoleType.ADMIN),
            Account(2, "user@example.com", "user", "user", RoleType.USER),
            Account(1000, "test@example.com", "test", "test", RoleType.USER),
        ).let { createAccount(it) }
    }

    private fun String.encode() = BCryptPasswordEncoder().encode(this)
}
