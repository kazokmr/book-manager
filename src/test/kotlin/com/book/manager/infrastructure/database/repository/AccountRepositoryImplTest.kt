package com.book.manager.infrastructure.database.repository

import com.book.manager.domain.enum.RoleType
import com.book.manager.domain.repository.AccountRepository
import com.book.manager.infrastructure.database.mapper.AccountMapper
import com.book.manager.infrastructure.database.mapper.deleteByPrimaryKey
import com.book.manager.infrastructure.database.record.AccountRecord
import com.book.manager.infrastructure.database.testcontainers.TestContainerPostgres
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.context.annotation.Import

@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(AccountRepositoryImpl::class)
internal class AccountRepositoryImplTest : TestContainerPostgres() {

    @Autowired
    private lateinit var accountRepository: AccountRepository

    @Autowired
    private lateinit var accountMapper: AccountMapper

    private lateinit var accountRecord: AccountRecord

    @BeforeEach
    internal fun setUp() {
        accountRecord = AccountRecord(999, "user@example.com", "pass", "user", RoleType.USER)
    }

    @Test
    @DisplayName("登録されているアカウントをIDで検索できる")
    fun `findById when account is exist then find by ID`() {

        // Given
        accountMapper.insertRecord(accountRecord)

        // When
        val account = accountRepository.findById(accountRecord.id!!)

        // Then
        assertThat(account).isNotNull
        SoftAssertions().apply {
            assertThat(account?.id).isEqualTo(accountRecord.id)
            assertThat(account?.email).isEqualTo(accountRecord.email)
            assertThat(account?.password).isEqualTo(accountRecord.password)
            assertThat(account?.name).isEqualTo(accountRecord.name)
            assertThat(account?.roleType).isEqualTo(accountRecord.roleType)
        }.assertAll()
    }

    @Test
    @DisplayName("登録されていないアカウントIDでは検索できないこと")
    fun `findById when account is not exist then can not find by ID`() {

        // Given
        val accountId = 777L
        accountMapper.deleteByPrimaryKey(accountId)

        // When
        val account = accountRepository.findById(accountId)

        // Then
        assertThat(account).isNull()
    }

    @Test
    @DisplayName("登録されているアカウントをEmailで検索できる")
    fun `findByEmail when account is exist then find by Email`() {

        // Given
        accountMapper.insertRecord(accountRecord)

        // When
        val account = accountRepository.findByEmail(accountRecord.email!!)

        // Then
        assertThat(account).isNotNull
        SoftAssertions().apply {
            assertThat(account?.id).isEqualTo(accountRecord.id)
            assertThat(account?.email).isEqualTo(accountRecord.email)
            assertThat(account?.password).isEqualTo(accountRecord.password)
            assertThat(account?.name).isEqualTo(accountRecord.name)
            assertThat(account?.roleType).isEqualTo(accountRecord.roleType)
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