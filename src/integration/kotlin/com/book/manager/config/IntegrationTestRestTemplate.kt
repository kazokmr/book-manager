package com.book.manager.config

import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import java.net.URI

class IntegrationTestRestTemplate(builder: RestTemplateBuilder) : TestRestTemplate(builder) {

    fun login(port: Int, user: String? = "user@example.com", pass: String? = "user"): ResponseEntity<String> {

        val loginForm = LinkedMultiValueMap<String, String>().apply {
            add("email", user)
            add("pass", pass)
        }

        val request = RequestEntity
            .post(URI.create("http://localhost:$port/login"))
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .accept(MediaType.TEXT_HTML)
            .body(loginForm)

        return restTemplate.exchange(request, String::class.java)
    }
}