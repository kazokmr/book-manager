package com.book.manager.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.web.reactive.function.client.ClientRequest
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeFunction
import reactor.core.publisher.Mono

private val logger: Logger = LoggerFactory.getLogger(CustomExchangeFilterFunction::class.java)

class CustomExchangeFilterFunction : ExchangeFilterFunction {

    private var sessionId = ""
    private var csrfToken = ""
    private var csrfHeader = "X-CSRF-TOKEN"

    override fun filter(request: ClientRequest, next: ExchangeFunction): Mono<ClientResponse> {

        val filteredRequest = ClientRequest.from(request)
            .cookies { cookies ->
                if (sessionId != "") {
                    logger.info("Set Cookie SESSION: $sessionId")
                    cookies.set("SESSION", sessionId)
                }
            }
            .headers { headers ->
                if (csrfToken != "") {
                    logger.info("Set Request Header $csrfHeader: $csrfToken")
                    headers.add(csrfHeader, csrfToken)
                }
            }
            .build()

        logger.info("Request: uri=${filteredRequest.url()}, headers=${filteredRequest.headers()}, body=${filteredRequest.body()}")

        return next.exchange(filteredRequest).doOnSuccess {

            logger.info("Response: status=${it.statusCode()}")

            val sessionIdFromCookie = it.cookies()["SESSION"]
            if (sessionIdFromCookie != null) {
                logger.info("Extracted cookie SESSION: ${sessionIdFromCookie[0]}")
                sessionId = sessionIdFromCookie[0].value
            }

            val headers: HttpHeaders = it.headers().asHttpHeaders()
            val csrfHeaders = headers["_csrf_header"]
            if (csrfHeaders != null) {
                logger.info("Extracted CSRF-HEADER from response: ${csrfHeaders[0]}")
                csrfHeader = csrfHeaders[0].toString()
            }
            val csrfTokens = headers["_csrf"]
            if (csrfTokens != null) {
                logger.info("Extracted CSRF-TOKEN from response: ${csrfTokens[0]}")
                csrfToken = csrfTokens[0].toString()
            }
        }
    }
}