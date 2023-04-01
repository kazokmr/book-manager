package com.book.manager.application.service

import com.book.manager.domain.model.Account
import com.book.manager.domain.repository.AccountRepository
import io.micrometer.observation.annotation.Observed
import org.springframework.stereotype.Service

@Observed
@Service
class AuthenticationService(private val accountRepository: AccountRepository) {
    fun findAccount(email: String): Account? = accountRepository.findByEmail(email)
}