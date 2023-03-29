package com.book.manager.presentation.config

import com.book.manager.application.service.security.BookManagerUserDetails
import com.book.manager.application.service.security.BookManagerUserMixin
import com.fasterxml.jackson.databind.ObjectMapper
import io.lettuce.core.metrics.MicrometerCommandLatencyRecorder
import io.lettuce.core.metrics.MicrometerOptions
import io.lettuce.core.resource.ClientResources
import io.micrometer.core.instrument.MeterRegistry
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializer
import org.springframework.security.jackson2.SecurityJackson2Modules
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession

@EnableRedisHttpSession
@Configuration
class HttpSessionConfig {

    @Value("\${spring.data.redis.host}")
    val redisHostName = "localhost"

    @Value("\${spring.data.redis.port}")
    val redisPort = 6379

    // Redis ClientのLettuceを利用するためのコネクション
    @Bean
    fun redisConnectionFactory(meterRegistry: MeterRegistry): LettuceConnectionFactory {
        val configuration = RedisStandaloneConfiguration(redisHostName, redisPort)
        val clientResources: ClientResources = ClientResources.builder()
            .commandLatencyRecorder(MicrometerCommandLatencyRecorder(meterRegistry, MicrometerOptions.create()))
            .build()
        val lettuceClientConfiguration: LettuceClientConfiguration = LettuceClientConfiguration.builder()
            .clientResources(clientResources)
            .build()
        return LettuceConnectionFactory(configuration, lettuceClientConfiguration)
    }

    // Redisのセッション情報をシリアライズする
    @Bean
    fun springSessionDefaultRedisSerializer(): RedisSerializer<Any> =
        ObjectMapper().apply {
            registerModules(SecurityJackson2Modules.getModules(this.javaClass.classLoader))
            addMixIn(BookManagerUserDetails::class.java, BookManagerUserMixin::class.java)
        }.let { Jackson2JsonRedisSerializer(it, Any::class.java) }
}
