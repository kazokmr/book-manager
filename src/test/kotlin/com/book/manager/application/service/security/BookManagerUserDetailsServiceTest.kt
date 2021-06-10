package com.book.manager.application.service.security

import com.book.manager.application.service.AuthenticationService
import com.book.manager.domain.enum.RoleType
import com.book.manager.domain.model.Account
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

internal class BookManagerUserDetailsServiceTest {

    private val authenticationService = mock<AuthenticationService>()
    private val bookManagerUserDetailsService = BookManagerUserDetailsService(authenticationService)

    @Test
    @DisplayName("アカウントが無ければNullを返す")
    fun `loadUserByUsername when account is null then return null`() {
        whenever(authenticationService.findAccount(any())).thenReturn(null)
        val result = bookManagerUserDetailsService.loadUserByUsername("user")
        assertThat(result).isNull()
    }

    @Test
    @DisplayName("アカウントが検索できれば、ユーザー情報を返す")
    fun `loadUserByUsername when account is not null then return BookManagerUserDetails`() {
        val userName = "moyomoyo"
        val account = Account(100L, "test@exmaple.com", "pass", userName, RoleType.USER)
        whenever(authenticationService.findAccount(any())).thenReturn(account)

        val result = bookManagerUserDetailsService.loadUserByUsername(userName)

        assertThat(result).isEqualTo(BookManagerUserDetails(account))
    }
}