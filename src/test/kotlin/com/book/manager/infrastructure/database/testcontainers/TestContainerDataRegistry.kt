package com.book.manager.infrastructure.database.testcontainers

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

@Testcontainers
abstract class TestContainerDataRegistry {

    companion object {
        @Container
        @JvmStatic
        val database = PostgreSQLContainer<Nothing>(DockerImageName.parse("postgres:latest")).apply {
            withDatabaseName("test")
            withUsername("user")
            withPassword("pass")
            withEnv("POSTGRES_INITDB_ARGS", "--encoding=UTF-8 --no-locale")
            withEnv("TZ", "Asia/Tokyo")
            withInitScript("initdb/schema.sql")
        }

        @Container
        @JvmStatic
        val redis: GenericContainer<*> = GenericContainer<Nothing>(DockerImageName.parse("redis:latest")).apply {
            withExposedPorts(6379)
        }

        @DynamicPropertySource
        @JvmStatic
        fun setUp(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", database::getJdbcUrl)
            registry.add("spring.datasource.username", database::getUsername)
            registry.add("spring.datasource.password", database::getPassword)
            registry.add("spring.data.redis.host", redis::getHost)
            registry.add("spring.data.redis.port", redis::getFirstMappedPort)
        }
    }
}
