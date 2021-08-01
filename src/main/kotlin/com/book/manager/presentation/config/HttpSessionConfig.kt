package com.book.manager.presentation.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession

@EnableRedisHttpSession
class HttpSessionConfig {

    @Value("\${spring.redis.host}")
    val redisHostName = "localhost"

    @Value("\${spring.redis.port}")
    val redisPort = 6379

    @Bean
    fun connectionFactory(): JedisConnectionFactory {
        val redisConfiguration = RedisStandaloneConfiguration().apply {
            hostName = redisHostName
            port = redisPort
        }
        return JedisConnectionFactory(redisConfiguration)
    }
}