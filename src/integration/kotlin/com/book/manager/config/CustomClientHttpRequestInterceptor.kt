package com.book.manager.config

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

    private var sessionId = ""
    private var csrfToken = ""

    // 認証エラーとならないようにセッションIDとCSRFトークンをリクエストにセットする
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {

        if (sessionId != "") {
            logger.info("Set Cookie SESSION: $sessionId")
            request.headers.add(HttpHeaders.COOKIE, "SESSION=$sessionId")
        }

        if (csrfToken != "") {
            logger.info("Set Request Header X-CSRF-TOKEN: $csrfToken")
            request.headers["X-CSRF-TOKEN"] = csrfToken
        }

        logger.info("Request: uri=${request.uri}, headers=${request.headers}, body=${String(body)}")
        val response = execution.execute(request, body)
        logger.info("Response: status=${response.statusCode}")

        val sessionByCookie = response.headers[HttpHeaders.SET_COOKIE]?.flatMap { HttpCookie.parse(it) }
            ?.firstOrNull { it.name == "SESSION" }
        if (sessionByCookie != null) {
            logger.info("Extracted cookie SESSION: $sessionByCookie")
            sessionId = sessionByCookie.value
        }

        if (response.headers.containsKey("_csrf")) {
            logger.info("Extracted CSRF-TOKEN from response: ${response.headers["_csrf"]?.get(0)}")
            csrfToken = response.headers["_csrf"]?.get(0).toString()
        }
        return response
    }
}