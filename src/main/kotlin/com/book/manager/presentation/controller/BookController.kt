package com.book.manager.presentation.controller

import com.book.manager.application.service.BookService
import com.book.manager.presentation.form.BookInfo
import com.book.manager.presentation.form.GetBookDetailResponse
import com.book.manager.presentation.form.GetBookListResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("book")
@CrossOrigin
class BookController(private val bookService: BookService) {
    @GetMapping("/list")
    fun getList(): GetBookListResponse = bookService.getList().map { BookInfo(it) }.let { GetBookListResponse(it) }

    @GetMapping("detail/{book_id}")
    fun getDetail(@PathVariable("book_id") bookId: Long): GetBookDetailResponse =
        kotlin.runCatching {
            bookService.getDetail(bookId)
        }.fold(
            onSuccess = { GetBookDetailResponse(it) },
            onFailure = { throw ResponseStatusException(HttpStatus.BAD_REQUEST, it.message) }
        )
}