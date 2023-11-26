package com.book.manager.presentation.handler

import com.book.manager.presentation.security.CsrfTokenHandler
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.csrf.CsrfToken

class BookManagerAuthenticationSuccessHandler : AuthenticationSuccessHandler {
    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        response.status = HttpServletResponse.SC_OK
        // 認証でセッションが書き換わるのでCSRFトークンもロードしなおす
        val csrfToken = request.getAttribute("_csrf") as CsrfToken
        CsrfTokenHandler(request, response).setToken(csrfToken)
    }
}
