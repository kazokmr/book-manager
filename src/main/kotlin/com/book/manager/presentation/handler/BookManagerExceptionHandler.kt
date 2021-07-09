package com.book.manager.presentation.handler

import com.fasterxml.jackson.databind.exc.InvalidFormatException
import com.fasterxml.jackson.module.kotlin.MissingKotlinParameterException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
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
     * Non-Nullableなプロパティにマッピングするパラメータが不正だった場合のエラーメッセージを作成する
     * */
    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest
    ): ResponseEntity<Any> {
        val body = when (val cause = ex.cause) {
            is MissingKotlinParameterException -> {
                cause.path.joinToString(",") { it.fieldName }
                    .let { mapOf(Pair("入力パラメータがありません", it)) }
            }
            is InvalidFormatException -> {
                cause.path.joinToString(",") {
                    "type of ${it.fieldName} should be ${cause.targetType}. but value was ${cause.value}"
                }.let { mapOf(Pair("入力パラメータの型が一致しません", it)) }
            }
            else -> mapOf(Pair("予期せぬエラー ${cause?.javaClass?.name}", ex.localizedMessage))
        }
        return handleExceptionInternal(ex, body, headers, status, request)
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