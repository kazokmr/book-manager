package com.book.manager.presentation.controller

import com.book.manager.application.service.RentalService
import com.book.manager.application.service.security.BookManagerUserDetails
import com.book.manager.presentation.form.RentalStartRequest
import org.springframework.http.HttpStatus
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("rental")
@CrossOrigin
class RentalController(private val rentalService: RentalService) {

    @PostMapping("/start")
    fun startRental(@RequestBody request: RentalStartRequest) {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as BookManagerUserDetails
        try {
            rentalService.startRental(request.bookId, userDetails.id)
        } catch (e: IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }

    @DeleteMapping("/end/{bookId}")
    fun endRental(@PathVariable("bookId") bookId: Long) {
        val userDetails = SecurityContextHolder.getContext().authentication.principal as BookManagerUserDetails
        try {
            rentalService.endRental(bookId, userDetails.id)
        } catch (e: java.lang.IllegalArgumentException) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, e.message)
        }
    }
}