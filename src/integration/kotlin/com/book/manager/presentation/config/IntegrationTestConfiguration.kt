package com.book.manager.presentation.config

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean

@TestConfiguration
class IntegrationTestConfiguration {

    @Bean
    fun integrationTestRestTemplate(): IntegrationTestRestTemplate {
        val builder = RestTemplateBuilder(CustomRestTemplateCustomizer())
        return IntegrationTestRestTemplate(builder)
    }
}