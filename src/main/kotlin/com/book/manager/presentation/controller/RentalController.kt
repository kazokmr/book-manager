package com.book.manager.presentation.controller

import com.book.manager.application.service.RentalService
import com.book.manager.application.service.security.BookManagerUserDetails
import com.book.manager.presentation.form.RentalStartRequest
import com.book.manager.presentation.form.RentalStartResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import javax.validation.Valid

@RestController
@RequestMapping("rental")
@CrossOrigin
class RentalController(private val rentalService: RentalService) {

    @PostMapping("/start")
    @ResponseStatus(HttpStatus.CREATED)
    fun startRental(
        @AuthenticationPrincipal userDetails: BookManagerUserDetails,
        @RequestBody @Valid request: RentalStartRequest
    ): RentalStartResponse =
        kotlin.runCatching {
            rentalService.startRental(request.bookId, userDetails.id)
        }.fold(
            onSuccess = { RentalStartResponse(it) },
            onFailure = { throw ResponseStatusException(HttpStatus.BAD_REQUEST, it.message) }
        )

    @DeleteMapping("/end/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun endRental(@AuthenticationPrincipal userDetails: BookManagerUserDetails, @PathVariable("bookId") bookId: Long) {
        kotlin.runCatching {
            rentalService.endRental(bookId, userDetails.id)
        }.onFailure { throw ResponseStatusException(HttpStatus.BAD_REQUEST, it.message) }
    }
}