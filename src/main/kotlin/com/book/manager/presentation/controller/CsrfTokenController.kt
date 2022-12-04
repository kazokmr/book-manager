package com.book.manager.presentation.controller

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin
class CsrfTokenController() {

    @GetMapping("/csrf_token")
    fun csrfToken(request: HttpServletRequest, response: HttpServletResponse): String {
        val csrfToken = request.getAttribute("_csrf") as CsrfToken
        response.setHeader("_csrf", csrfToken.token)
        return "Get CSRF TOKEN"
    }
}