package com.book.manager.application.service.security

import com.book.manager.application.service.AuthenticationService
import com.book.manager.domain.enum.RoleType
import com.book.manager.domain.model.Account
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.security.core.userdetails.UsernameNotFoundException

@ExtendWith(MockitoExtension::class)
internal class BookManagerUserDetailsServiceTest {

    @InjectMocks
    private lateinit var bookManagerUserDetailsService: BookManagerUserDetailsService

    @Mock
    private lateinit var authenticationService: AuthenticationService

    @Test
    @DisplayName("アカウントが無ければ UsernameNotFoundExceptionを返す")
    fun `loadUserByUsername when account is null then return null`() {

        // Given
        val username = "invalidUser"
        whenever(authenticationService.findAccount(any())).thenReturn(null)

        // When
        val result = assertThrows<UsernameNotFoundException> {
            bookManagerUserDetailsService.loadUserByUsername(username)
        }

        // Then
        verify(authenticationService).findAccount(username)
        assertThat(result.message).isEqualTo("無効なユーザー名です $username")
    }

    @Test
    @DisplayName("アカウントが検索できれば、ユーザー情報を返す")
    fun `loadUserByUsername when account is not null then return BookManagerUserDetails`() {

        // Given
        val userName = "moyomoyo"
        val account = Account(100L, "test@exmaple.com", "pass", userName, RoleType.USER)
        whenever(authenticationService.findAccount(any())).thenReturn(account)

        // When
        val result = bookManagerUserDetailsService.loadUserByUsername(userName) as BookManagerUserDetails

        // Then
        assertThat(result).isNotNull
        SoftAssertions().apply {
            assertThat(result.id).`as`("id").isEqualTo(account.id)
            assertThat(result.email).`as`("email").isEqualTo(account.email)
            assertThat(result.name).`as`("name").isEqualTo(account.name)
            assertThat(result.username).`as`("username").isEqualTo(account.email)
            assertThat(result.password).`as`("password").isEqualTo(account.password)
            assertThat(result.roleType).`as`("roleType").isEqualTo(account.roleType)
            assertThat(result.authorities.size).`as`("authoritiesSize").isEqualTo(1)
            assertThat(result.isAccountNonExpired).`as`("isAccountNonExpired").isTrue
            assertThat(result.isAccountNonLocked).`as`("isAccountNonLocked").isTrue
            assertThat(result.isCredentialsNonExpired).`as`("isCredentialsNonExpired").isTrue
            assertThat(result.isEnabled).`as`("isEnabled").isTrue
        }.assertAll()
    }
}
