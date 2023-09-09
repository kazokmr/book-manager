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
abstract class TestContainerDataRegistry {

    companion object {
        @Container
        @ServiceConnection
        @JvmStatic
        val database = PostgreSQLContainer<Nothing>(DockerImageName.parse("postgres").withTag("latest")).apply {
            withEnv("POSTGRES_INITDB_ARGS", "--encoding=UTF-8 --no-locale")
            withEnv("TZ", "Asia/Tokyo")
            withInitScript("initdb/schema.sql")
        }

        @Container
        @ServiceConnection
        @JvmStatic
        val redis: GenericContainer<*> =
            GenericContainer<Nothing>(DockerImageName.parse("redis").withTag("latest")).apply {
                withExposedPorts(6379)
            }

        @DynamicPropertySource
        @JvmStatic
        fun setUp(registry: DynamicPropertyRegistry) {
            // FIXME: RedisのportだけはDynamicPropertySourceで定義しないとアクセスしてくれない
            registry.add("spring.data.redis.port", redis::getFirstMappedPort)
        }
    }
}
