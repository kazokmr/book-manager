package com.book.manager.config

import com.book.manager.domain.enum.RoleType
import com.book.manager.domain.model.Account
import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import com.book.manager.domain.model.Rental
import com.book.manager.infrastructure.database.mapper.AccountMapper
import com.book.manager.infrastructure.database.mapper.BookMapper
import com.book.manager.infrastructure.database.mapper.RentalMapper
import com.book.manager.infrastructure.database.mapper.delete
import com.book.manager.infrastructure.database.mapper.insertMultiple
import com.book.manager.infrastructure.database.record.AccountRecord
import com.book.manager.infrastructure.database.record.BookRecord
import com.book.manager.infrastructure.database.record.RentalRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestComponent
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

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
            .map { AccountRecord(it.id, it.email, it.password.encode(), it.name, it.roleType) }
            .forEach { accountMapper.insertRecord(it) }
    }

    fun createBook(bookList: List<Book>) {
        bookList
            .map { BookRecord(it.id, it.title, it.author, it.releaseDate) }
            .let { bookMapper.insertMultiple(it) }
    }

    fun createRental(rentalList: List<Rental>) {
        rentalList
            .map { RentalRecord(it.bookId, it.accountId, it.rentalDatetime, it.rentalDatetime) }
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