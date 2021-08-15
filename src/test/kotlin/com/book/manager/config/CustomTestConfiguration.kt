package com.book.manager.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class CustomTestConfiguration {

    @Bean
    fun jsonConverter(): CustomJsonConverter = CustomJsonConverter()
}