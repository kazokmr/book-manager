package com.book.manager.application.service

import com.book.manager.domain.model.Account
import com.book.manager.domain.repository.AccountRepository
import org.springframework.stereotype.Service

@Service
class AuthenticationService(private val accountRepository: AccountRepository) {
    fun findAccount(email: String): Account? {
        return accountRepository.findByEmail(email)
    }
}