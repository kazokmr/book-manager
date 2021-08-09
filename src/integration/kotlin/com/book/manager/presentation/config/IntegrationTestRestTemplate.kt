package com.book.manager.presentation.config

import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.util.LinkedMultiValueMap
import java.net.URI

class IntegrationTestRestTemplate : TestRestTemplate() {

    fun login(port: Int, _user: String?, _pass: String?): ResponseEntity<String> {
        val user = _user ?: "user@example.com"
        val pass = _pass ?: "user"

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

    fun preLogin(port: Int, _user: String?, _pass: String?): HttpHeaders {
        val response = login(port, _user, _pass)
        val cookies = response.headers[HttpHeaders.SET_COOKIE]
        val httpHeaders = HttpHeaders()
        cookies?.forEach { httpHeaders.add("Cookie", it) }
        return httpHeaders
    }
}