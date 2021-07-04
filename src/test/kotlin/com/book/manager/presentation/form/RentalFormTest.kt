package com.book.manager.presentation.form

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import javax.validation.Validation

internal class RentalFormTest {

    private val validator = Validation.buildDefaultValidatorFactory().validator

    @Test
    fun `RentalStartRequest when bookId is 0 then return violation`() {

        // Given
        val rentalStartRequest = RentalStartRequest(0L)

        // When
        val violations = validator.validate(rentalStartRequest)

        // Then
        assertThat(violations.size).isEqualTo(1)
        assertThat(violations).extracting("message").containsOnly("書籍IDには1以上の数値を入れてください。")
    }
}