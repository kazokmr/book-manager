package com.book.manager.presentation.config

import com.book.manager.application.service.AdminBookService
import com.book.manager.application.service.AuthenticationService
import com.book.manager.application.service.mockuser.WithCustomMockUser
import com.book.manager.domain.enum.RoleType
import com.book.manager.domain.model.Account
import com.book.manager.presentation.controller.AdminBookController
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(controllers = [AdminBookController::class])
internal class SecurityConfigTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @MockBean
    private lateinit var authenticationService: AuthenticationService

    // AdminBookはUSERロールではアクセス出来ない仕様なのでテスト
    @MockBean
    private lateinit var adminBookService: AdminBookService

    @Test
    @DisplayName("ユーザー名とパスワードが一致すればログイン認証に成功する")
    fun `formLogin when account exists then success authentication`() {

        // Given
        val email = "test@example.com"
        val pass = "passpass"
        val role = RoleType.USER

        val account = Account(1, email, passwordEncoder.encode(pass), "test", role)
        whenever(authenticationService.findAccount(any() as String)).thenReturn(account)

        // When
        mockMvc
            .perform(
                formLogin()
                    .loginProcessingUrl("/login")
                    .user("email", email)
                    .password("pass", pass)
            )
            .andExpect(authenticated().withUsername(email))
            .andExpect(status().isOk)
    }

    @Test
    @DisplayName("パスワードが違うとログイン認証に失敗する")
    fun `formLogin when password is incorrectly then failure authentication`() {

        // Given
        val email = "test@example.com"
        val pass = "passpass"
        val role = RoleType.USER
        val account =
            Account(1, email, passwordEncoder.encode(pass), "test", role)
        whenever(authenticationService.findAccount(any() as String)).thenReturn(account)

        // When
        mockMvc
            .perform(
                formLogin()
                    .loginProcessingUrl("/login")
                    .user("email", email)
                    .password("pass", "invalid")
            )
            .andExpect(unauthenticated())
            .andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("登録されていないユーザー名ならログイン認証に失敗する")
    fun `formLogin when account does not exist then failure authentication`() {

        // Given
        whenever(authenticationService.findAccount(any() as String)).thenReturn(null)

        // When
        mockMvc
            .perform(
                formLogin()
                    .loginProcessingUrl("/login")
                    .user("email", "unregister")
                    .password("pass", "invalid")
            )
            .andExpect(unauthenticated())
            .andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("認証されていなければアクセスできない")
    fun `exceptionHandling when account does not authenticate then can not access except login`() {

        // When
        mockMvc
            .perform(
                delete("/admin/book/delete/100")
                    .with(csrf().asHeader())
            )
            .andExpect(unauthenticated())
            .andExpect(status().isUnauthorized)
    }

    @Test
    @DisplayName("認証アカウントが権限を持たなければアクセスできない")
    @WithCustomMockUser(roleType = RoleType.USER)
    fun `exceptionHandling when account has not authorization then can not access`() {

        // When
        mockMvc
            .perform(
                delete("/admin/book/delete/200")
                    .with(csrf().asHeader())
            )
            .andExpect(status().isForbidden)
    }
}