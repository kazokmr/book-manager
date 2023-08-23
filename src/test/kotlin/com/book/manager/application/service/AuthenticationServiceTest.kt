package com.book.manager.application.service

import com.book.manager.domain.enum.RoleType
import com.book.manager.domain.model.Account
import com.book.manager.domain.repository.AccountRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class AuthenticationServiceTest {

    @InjectMocks
    private lateinit var authenticationService: AuthenticationService

    @Mock
    private lateinit var accountRepository: AccountRepository

    @Test
    @DisplayName("EmailでDB検索を行うこと")
    fun `findAccount when call with email then read db with email`() {

        // Given
        val email = "test@example.com"

        // When
        authenticationService.findAccount(email)

        // Then
        verify(accountRepository).findByEmail(email)
    }

    @Test
    @DisplayName("Emailからユーザーを検索する")
    fun `findAccount when email is not null then return account`() {

        // Given
        val email = "test@example.com"
        val account = Account(100L, email, "pass", "hogehoge", RoleType.USER)
        whenever(accountRepository.findByEmail(any())).thenReturn(account)

        // When
        val result = authenticationService.findAccount(email)

        // Then
        assertThat(result).isEqualTo(account)
    }

    @Test
    @DisplayName("Emailに該当するユーザーがいなければNullを返す")
    fun `findAccount when account does not have email return null`() {

        // Given
        val email = "test@example.com"
        whenever(accountRepository.findByEmail(any())).thenReturn(null)

        // When
        val result = authenticationService.findAccount(email)

        // Then
        assertThat(result).isNull()
    }
}
