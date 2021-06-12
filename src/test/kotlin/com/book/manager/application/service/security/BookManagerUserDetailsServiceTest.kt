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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory

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

@Retention(AnnotationRetention.RUNTIME)
@WithSecurityContext(factory = WithMockCustomUserSecurityContextFactory::class)
annotation class WithCustomMockUser(
    val id: Long = 1000L,
    val email: String = "test@example.com",
    val pass: String = "pass",
    val username: String = "test",
    val roleType: RoleType = RoleType.USER
) {}

class WithMockCustomUserSecurityContextFactory() : WithSecurityContextFactory<WithCustomMockUser> {
    override fun createSecurityContext(user: WithCustomMockUser): SecurityContext {
        val account = Account(user.id, user.email, user.pass, user.username, user.roleType)
        val principal = BookManagerUserDetails(account)
        val auth = UsernamePasswordAuthenticationToken(principal, principal.password, principal.authorities)
        val context = SecurityContextHolder.createEmptyContext()
        context.authentication = auth
        return context
    }

}