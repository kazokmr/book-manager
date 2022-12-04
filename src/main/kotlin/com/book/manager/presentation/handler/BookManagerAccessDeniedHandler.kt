package com.book.manager.presentation.handler

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler

private val logger: Logger = LogManager.getLogger(BookManagerAccessDeniedHandler::class)


class BookManagerAccessDeniedHandler : AccessDeniedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        accessDeniedException: AccessDeniedException
    ) {
        logger.info(accessDeniedException.localizedMessage)
        response.status = HttpServletResponse.SC_FORBIDDEN
    }

}
