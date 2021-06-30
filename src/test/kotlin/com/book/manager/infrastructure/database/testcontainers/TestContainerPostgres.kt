package com.book.manager.infrastructure.database.testcontainers

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
abstract class TestContainerPostgres {

    companion object {
        @JvmStatic
        val container = PostgreSQLContainer<Nothing>(DockerImageName.parse("postgres")).apply {
            withDatabaseName("test")
            withUsername("user")
            withPassword("pass")
            withEnv("POSTGRES_INITDB_ARGS", "--encoding=UTF-8 --no-locale")
            withEnv("TZ", "Asia/Tokyo")
            withInitScript("initdb/schema.sql")
            withReuse(true)
            start()
        }

        @DynamicPropertySource
        @JvmStatic
        fun setUp(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", container::getJdbcUrl)
            registry.add("spring.datasource.username", container::getUsername)
            registry.add("spring.datasource.password", container::getPassword)
        }
    }
}