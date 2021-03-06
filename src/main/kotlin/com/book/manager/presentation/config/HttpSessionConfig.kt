package com.book.manager.presentation.config

import com.book.manager.application.service.security.BookManagerUserDetails
import com.book.manager.application.service.security.BookManagerUserMixin
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.security.jackson2.SecurityJackson2Modules

// Redisでセッション管理を有効にする(application.yamlで session-storetype = redis としているのでこの設定は見ていない)
//@EnableRedisHttpSession
@Configuration
class HttpSessionConfig {

    @Value("\${spring.redis.host}")
    val redisHostName = "localhost"

    @Value("\${spring.redis.port}")
    val redisPort = 6379

    // Redis ClientのLettuceを利用するためのコネクション
    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory =
        LettuceConnectionFactory(RedisStandaloneConfiguration(redisHostName, redisPort))

    // Redisのセッション情報をシリアライズ処理(com.fasterxml.jackson.databind.J)
    @Bean
    fun springSessionDefaultRedisSerializer(): RedisSerializer<Any> =
        Jackson2JsonRedisSerializer(Any::class.java).apply {
            val objectMapper = ObjectMapper()
            objectMapper.registerModules(SecurityJackson2Modules.getModules(this.javaClass.classLoader))
            objectMapper.addMixIn(BookManagerUserDetails::class.java, BookManagerUserMixin::class.java)
            setObjectMapper(objectMapper)
        }
}
