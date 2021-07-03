package com.book.manager.presentation.controller

import com.book.manager.application.service.AdminBookService
import com.book.manager.application.service.result.Result
import com.book.manager.domain.model.Book
import com.book.manager.presentation.form.AdminBookResponse
import com.book.manager.presentation.form.RegisterBookRequest
import com.book.manager.presentation.form.UpdateBookRequest
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

@RestController
@RequestMapping("admin/book")
@CrossOrigin
class AdminBookController(private val adminBookService: AdminBookService) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@RequestBody request: RegisterBookRequest): AdminBookResponse =
        when (val result =
            adminBookService.register(Book(request.id, request.title, request.author, request.releaseDate))) {
            is Result.Success -> AdminBookResponse(result.data as Book)
            is Result.Failure -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, result.message)
        }

    @PutMapping("/update")
    fun update(@RequestBody request: UpdateBookRequest): AdminBookResponse =
        when (val result =
            adminBookService.update(request.id, request.title, request.author, request.releaseDate)) {
            is Result.Success -> AdminBookResponse(result.data as Book)
            is Result.Failure -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, result.message)
        }

    @DeleteMapping("/delete/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable("bookId") bookId: Long) {
        when (val result = adminBookService.delete(bookId)) {
            is Result.Success -> result.data
            is Result.Failure -> throw ResponseStatusException(HttpStatus.BAD_REQUEST, result.message)
        }
    }
}