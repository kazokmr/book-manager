package com.book.manager.presentation.handler

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class BookManagerExceptionHandler : ResponseEntityExceptionHandler() {

    /**
     * RequestBodyのValidationメッセージをレスポンスに出力するためにフィールドとメッセージをマッピングします
     * */
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        val body = ex.bindingResult.allErrors.associateBy({ it.let { it as FieldError }.field }, { it.defaultMessage })
        return handleExceptionInternal(ex, body, headers, status, request)
    }

    /**
     * ResponseEntityExceptionHanderクラスの@ExceptionHandlerで定義されていない例外クラスを受け取る
     * */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = [Exception::class])
    fun handlerException(
        ex: Exception,
        headers: HttpHeaders,
        request: WebRequest
    ): ResponseEntity<Any> {
        return super.handleExceptionInternal(ex, null, headers, HttpStatus.BAD_REQUEST, request)
    }

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        val responseBody = body ?: BookManagerErrorResponse(ex.message ?: ex.localizedMessage)
        return super.handleExceptionInternal(ex, responseBody, headers, status, request)
    }
}

data class BookManagerErrorResponse(val message: String)