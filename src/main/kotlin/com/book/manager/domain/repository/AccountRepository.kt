package com.book.manager.domain.repository

import com.book.manager.domain.model.Account

interface AccountRepository {
    fun findByEmail(email: String): Account?
    fun findById(id: Long): Account?
}