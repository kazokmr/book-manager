package com.book.manager.presentation.security

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.web.csrf.CsrfToken

class CsrfTokenHandler(val request: HttpServletRequest, val response: HttpServletResponse) {

    fun setToken(csrfToken: CsrfToken) {
        response.setHeader("_csrf", csrfToken.token)
        response.setHeader("_csrf_header", csrfToken.headerName)
    }
}
