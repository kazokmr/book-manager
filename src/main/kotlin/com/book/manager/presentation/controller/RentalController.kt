package com.book.manager.presentation.controller

import com.book.manager.application.service.RentalService
import com.book.manager.application.service.result.Result
import com.book.manager.application.service.security.BookManagerUserDetails
import com.book.manager.presentation.form.RentalStartRequest
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
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
    fun startRental(
        @AuthenticationPrincipal userDetails: BookManagerUserDetails,
        @RequestBody request: RentalStartRequest
    ) {
        when (val result = rentalService.startRental(request.bookId, userDetails.id)) {
            is Result.Success -> result.data
            is Result.Failure -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, result.message)
        }
    }

    @DeleteMapping("/end/{bookId}")
    fun endRental(@AuthenticationPrincipal userDetails: BookManagerUserDetails, @PathVariable("bookId") bookId: Long) {
        when (val result = rentalService.endRental(bookId, userDetails.id)) {
            is Result.Success -> result.data
            is Result.Failure -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, result.message)
        }
    }
}