package com.book.manager.domain.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDate

data class Book(
    val id: Long,
    val title: String,
    val author: String,
    @JsonProperty("release_date") val releaseDate: LocalDate
)
