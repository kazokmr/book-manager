package com.book.manager.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class CustomJsonConverter {

    fun <T> toObject(json: String?, obj: Class<T>): T? =
        kotlin.runCatching {
            objectMapper().readValue(json, obj)
        }.fold(
            onSuccess = { it },
            onFailure = {
                println(it.message)
                null
            }
        )

    fun <T> toJson(obj: T): String = objectMapper().writeValueAsString(obj)

    fun objectMapper() = ObjectMapper().apply {
        registerKotlinModule()
        registerModule(JavaTimeModule())
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }
}