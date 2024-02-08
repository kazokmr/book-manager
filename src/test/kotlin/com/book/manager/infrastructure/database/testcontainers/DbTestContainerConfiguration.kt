package com.book.manager.infrastructure.database.testcontainers

import org.springframework.boot.testcontainers.service.connection.ServiceConnection
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName


@Testcontainers
class DbTestContainerConfiguration {

    companion object {

        @JvmStatic
        @Container
        @ServiceConnection
        val database = PostgreSQLContainer<Nothing>(DockerImageName.parse("postgres").withTag("latest")).apply {
            withEnv("postgres_initdb_args", "--encoding=utf-8 --no-locale")
            withEnv("tz", "asia/tokyo")
            withDatabaseName("book_manager")
            withInitScript("initdb/schema.sql")
        }

        @JvmStatic
        @Container
        @ServiceConnection(name = "redis")
        val redis: GenericContainer<*> =
            GenericContainer<Nothing>(DockerImageName.parse("redis").withTag("latest")).apply {
                withExposedPorts(6379)
            }

        @JvmStatic
        @DynamicPropertySource
        fun setUp(registry: DynamicPropertyRegistry) {
            // GenericContainer には `port` プロパティへのアクセッサが無いため redis.port に `firstMappedPort`プロパティを指定する
            registry.add("spring.data.redis.port", redis::getFirstMappedPort)
        }
    }
}
