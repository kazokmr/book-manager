package com.book.manager.presentation.controller

import com.book.manager.presentation.security.CsrfTokenHandler
import io.micrometer.observation.annotation.Observed
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.web.csrf.CsrfToken
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@Observed
@RestController
@CrossOrigin
class CsrfTokenController {

    @GetMapping("/csrf_token")
    fun csrfToken(request: HttpServletRequest, response: HttpServletResponse, csrfToken: CsrfToken): String {
        CsrfTokenHandler(request, response).setToken(csrfToken)
        return "Send Token!"
    }
}
