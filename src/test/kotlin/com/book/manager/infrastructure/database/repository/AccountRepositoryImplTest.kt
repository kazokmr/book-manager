package com.book.manager.infrastructure.database.repository

import com.book.manager.domain.enum.RoleType
import com.book.manager.domain.model.Account
import com.book.manager.domain.repository.AccountRepository
import com.book.manager.infrastructure.database.mapper.AccountMapper
import com.book.manager.infrastructure.database.testcontainers.TestContainerDataRegistry
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.test.context.jdbc.Sql

@MybatisTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql("Account.sql")
internal class AccountRepositoryImplTest : TestContainerDataRegistry() {

    @Autowired
    private lateinit var accountMapper: AccountMapper

    private lateinit var accountRepository: AccountRepository

    @BeforeEach
    internal fun setUp() {
        accountRepository = AccountRepositoryImpl(accountMapper)
    }

    @Test
    @DisplayName("登録されているアカウントをIDで検索できる")
    fun `findById when account is exist then find by ID`() {

        // Given
        val accountId = 1L
        val expectedAccount = Account(accountId, "admin@example.com", "passpass", "admin", RoleType.ADMIN)

        // When
        val resultAccount = accountRepository.findById(accountId)

        // Then
        assertThat(resultAccount).isNotNull
        SoftAssertions().apply {
            assertThat(resultAccount?.id).isEqualTo(expectedAccount.id)
            assertThat(resultAccount?.email).isEqualTo(expectedAccount.email)
            assertThat(resultAccount?.password).isEqualTo(expectedAccount.password)
            assertThat(resultAccount?.name).isEqualTo(expectedAccount.name)
            assertThat(resultAccount?.roleType).isEqualTo(expectedAccount.roleType)
        }.assertAll()
    }

    @Test
    @DisplayName("登録されていないアカウントIDでは検索できないこと")
    fun `findById when account is not exist then can not find by ID`() {

        // Given
        val accountId = 777L

        // When
        val account = accountRepository.findById(accountId)

        // Then
        assertThat(account).isNull()
    }

    @Test
    @DisplayName("登録されているアカウントをEmailで検索できる")
    fun `findByEmail when account is exist then find by Email`() {

        // Given
        val expectedAccount = Account(8888, "user2@example.com", "passpass", "user2", RoleType.USER)

        // When
        val account = accountRepository.findByEmail(expectedAccount.email)

        // Then
        assertThat(account).isNotNull
        SoftAssertions().apply {
            assertThat(account?.id).isEqualTo(expectedAccount.id)
            assertThat(account?.email).isEqualTo(expectedAccount.email)
            assertThat(account?.password).isEqualTo(expectedAccount.password)
            assertThat(account?.name).isEqualTo(expectedAccount.name)
            assertThat(account?.roleType).isEqualTo(expectedAccount.roleType)
        }.assertAll()
    }

    @Test
    @DisplayName("登録されていないアカウントのEmailでは検索できないこと")
    fun `findByEmail when account is not exist then can not find by Email`() {

        // Given
        val email = "unsubscribe@example.com"

        // When
        val account = accountRepository.findByEmail(email)

        // Then
        assertThat(account).isNull()
    }
}
