package com.book.manager.config

import org.springframework.boot.web.client.RestTemplateCustomizer
import org.springframework.web.client.RestTemplate

class CustomRestTemplateCustomizer : RestTemplateCustomizer {
    override fun customize(restTemplate: RestTemplate?) {
        restTemplate?.interceptors?.add(CustomClientHttpRequestInterceptor())
    }
}