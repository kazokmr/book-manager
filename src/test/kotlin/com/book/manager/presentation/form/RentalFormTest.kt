package com.book.manager.presentation.form

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import javax.validation.Validation
import javax.validation.Validator

internal class RentalFormTest {

    private lateinit var validator: Validator

    // parameterrizedTestの@MethodSource で使うstaticメソッド
    companion object {
        @Suppress("unused")
        @JvmStatic
        fun bookIds() = longArrayOf(1, 10, 100, 1000)
    }

    @BeforeEach
    internal fun setUp() {
        validator = Validation.buildDefaultValidatorFactory().validator
    }

    @ParameterizedTest(name = "書籍IDが0以下は入力エラー: BookID => {0}")
    @ValueSource(longs = [0, -1])
    fun `RentalStartRequest when bookId is less than 1 then return violation`(bookId: Long) {

        // Given
        val rentalStartRequest = RentalStartRequest(bookId)

        // When
        val violations = validator.validate(rentalStartRequest)

        // Then
        assertThat(violations.size).isEqualTo(1)
        assertThat(violations).extracting("message").containsOnly("書籍IDには1以上の数値を入れてください。")
    }

    @ParameterizedTest(name = "書籍IDが1以上なら入力OK: BookID => {0}")
    @MethodSource("bookIds")
    fun `RentalStartRequest when bookId is greater than 0 return no violation`(bookId: Long) {

        // Given
        val rentalStartRequest = RentalStartRequest(bookId)

        // When
        val violations = validator.validate(rentalStartRequest)

        // Then
        assertThat(violations.size).isEqualTo(0)
        assertThat(violations).extracting("message").isEmpty()
    }

}