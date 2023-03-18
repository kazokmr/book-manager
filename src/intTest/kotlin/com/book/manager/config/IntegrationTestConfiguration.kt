package com.book.manager.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class IntegrationTestConfiguration {

    @Bean
    fun exchangeFilter(): CustomExchangeFilterFunction = CustomExchangeFilterFunction()

    @Bean
    fun testMapper(): CustomTestMapper = CustomTestMapper()

    @Bean
    fun jsonConverter(): CustomJsonConverter = CustomJsonConverter()
}