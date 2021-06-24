package com.book.manager.application.service.result

sealed interface Result {
    data class Success(val data: Any) : Result
    data class Failure(val message: String) : Result
}
