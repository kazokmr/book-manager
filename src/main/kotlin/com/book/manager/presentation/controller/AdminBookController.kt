package com.book.manager.presentation.controller

import com.book.manager.application.service.AdminBookService
import com.book.manager.domain.model.Book
import com.book.manager.presentation.form.AdminBookResponse
import com.book.manager.presentation.form.RegisterBookRequest
import com.book.manager.presentation.form.UpdateBookRequest
import io.micrometer.observation.annotation.Observed
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@Observed
@RestController
@RequestMapping("admin/book")
@CrossOrigin
class AdminBookController(private val adminBookService: AdminBookService) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@RequestBody request: RegisterBookRequest): AdminBookResponse =
        kotlin.runCatching {
            adminBookService.register(Book(request.id, request.title, request.author, request.releaseDate))
        }.fold(
            onSuccess = { AdminBookResponse(it) },
            onFailure = { throw ResponseStatusException(HttpStatus.BAD_REQUEST, it.message) }
        )

    @PutMapping("/update")
    fun update(@RequestBody request: UpdateBookRequest): AdminBookResponse =
        kotlin.runCatching {
            adminBookService.update(request.id, request.title, request.author, request.releaseDate)
        }.fold(
            onSuccess = { AdminBookResponse(it, request.title, request.author, request.releaseDate) },
            onFailure = { throw ResponseStatusException(HttpStatus.BAD_REQUEST, it.message) }
        )

    @DeleteMapping("/delete/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable("bookId") bookId: Long) {
        kotlin.runCatching {
            adminBookService.delete(bookId)
        }.onFailure { throw ResponseStatusException(HttpStatus.BAD_REQUEST, it.message) }
    }
}