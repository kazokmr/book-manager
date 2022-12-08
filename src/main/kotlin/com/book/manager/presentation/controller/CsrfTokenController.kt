package com.book.manager.presentation.controller

import com.book.manager.presentation.security.CsrfTokenHandler
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin
class CsrfTokenController {

    @GetMapping("/csrf_token")
    fun csrfToken(request: HttpServletRequest, response: HttpServletResponse): String {
        CsrfTokenHandler(request, response).setToken()
        return "Send Token!"
    }
}