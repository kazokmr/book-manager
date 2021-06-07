package com.book.manager.presentation.form

import com.book.manager.domain.model.BookWithRental

data class GetBookListResponse(val bookList: List<BookInfo>)

data class BookInfo(val id: Long, val title: String, val author: String, val isRental: Boolean) {
    constructor(model: BookWithRental) : this(model.book.id, model.book.title, model.book.author, model.isRental)
}
