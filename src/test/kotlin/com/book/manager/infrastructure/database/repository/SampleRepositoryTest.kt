package com.book.manager.infrastructure.database.repository

import com.book.manager.domain.enum.RoleType
import com.book.manager.domain.repository.AccountRepository
import com.book.manager.infrastructure.database.mapper.AccountMapper
import com.book.manager.infrastructure.database.record.AccountRecord
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mybatis.spring.boot.test.autoconfigure.MybatisTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import kotlin.properties.Delegates

@MybatisTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import(AccountRepositoryImpl::class)
@Testcontainers
class SampleRepositoryTest {

    companion object {
        @Container
        @JvmStatic
        val postgres: PostgreSQLContainer<*> =
            PostgreSQLContainer<Nothing>(DockerImageName.parse("postgres")).apply {
                withDatabaseName("test")
                withUsername("user")
                withPassword("pass")
                withEnv("POSTGRES_INITDB_ARGS", "--encoding=UTF8 --no-locale")
                withEnv("TZ", "Asia/Tokyo")
                withInitScript("initdb/schema.sql")
            }

        @DynamicPropertySource
        @JvmStatic
        fun setUp(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }

    @Autowired
    private lateinit var accountMapper: AccountMapper

    @Autowired
    private lateinit var accountRepository: AccountRepository

    private var accountId by Delegates.notNull<Long>()

    @BeforeEach
    internal fun setUp() {
        accountId = 999
    }

    @Test
    fun `test when account is not exist then find no account`() {
        // When
        val account = accountRepository.findById(accountId)

        // then
        assertThat(account).isNull()
    }

    @Test
    fun `test when connect db container then run`() {

        // Given
        val record = AccountRecord(accountId, "test@example.com", "pass", "hogehoge", RoleType.ADMIN)
        accountMapper.create(record)

        // When
        val account = accountRepository.findById(accountId)

        // Then
        SoftAssertions().apply {
            assertThat(account?.id).isEqualTo(accountId)
            assertThat(account?.email).isEqualTo(record.email)
            assertThat(account?.password).isEqualTo(record.password)
            assertThat(account?.name).isEqualTo(record.name)
            assertThat(account?.roleType).isEqualTo(record.roleType)
        }.assertAll()
    }
}