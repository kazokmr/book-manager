package com.book.manager.domain.model

import com.book.manager.domain.enum.RoleType

data class Account(val id: Long, val email: String, val password: String, val name: String, val roleType: RoleType)
