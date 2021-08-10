package com.book.manager.presentation.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter

@TestConfiguration
class IntegrationTestConfiguration {

    @Bean
    fun integrationTestRestTemplate(): IntegrationTestRestTemplate {
        return IntegrationTestRestTemplate()
    }

    // TestRestTemplateを継承したクラスを使うとjackson-module-kotlinのConverterが効かなくなるためBeanを用意する
    @Bean
    fun mappingJackson2HttpMessageConverter(): MappingJackson2HttpMessageConverter {
        return MappingJackson2HttpMessageConverter().apply {
            this.objectMapper = ObjectMapper().apply {
                registerKotlinModule()
            }
        }
    }
}