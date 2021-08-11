package com.book.manager.presentation.config

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.net.HttpCookie

private val logger: Logger = LogManager.getLogger(CustomClientHttpRequestInterceptor::class)

class CustomClientHttpRequestInterceptor : ClientHttpRequestInterceptor {

    private var cookies = mutableMapOf<String, HttpCookie>()

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {

        // Requestヘッダーに直前のResponseヘッダーのCookieをセットしてから送信する
        val cookiesForRequest = cookies.values.map { it.toString() }.toList()
        logger.info("Using cookies: $cookiesForRequest")
        request.headers.addAll(HttpHeaders.COOKIE, cookiesForRequest)

        logger.info("Request: uri=${request.uri}, headers=${request.headers}, body=${String(body)}")
        val response = execution.execute(request, body)

        val cookiesFromResponse = response.headers[HttpHeaders.SET_COOKIE]?.flatMap { HttpCookie.parse(it) }
        logger.info("Extracted cookies from response: $cookiesFromResponse")
        cookiesFromResponse?.forEach { cookies[it.name] = it }

        return response
    }
}