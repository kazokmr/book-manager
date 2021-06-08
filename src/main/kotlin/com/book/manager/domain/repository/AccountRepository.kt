package com.book.manager.domain.repository

import com.book.manager.domain.model.Account

interface AccountRepository {
    fun find(email: String): Account?
    fun find(id: Long): Account?
}