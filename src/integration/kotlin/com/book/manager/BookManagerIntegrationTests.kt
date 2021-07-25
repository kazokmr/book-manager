package com.book.manager

import com.book.manager.domain.model.Book
import com.book.manager.domain.model.BookWithRental
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class BookManagerIntegrationTests {

    @Test
    @DisplayName("疎通テスト")
    fun smoke() {

        // Given
        val book = Book(100, "integration test", "integration", LocalDate.now())

        // When
        val bookWithRental = BookWithRental(book, null)

        // Then
        assertThat(bookWithRental.isRental).isFalse
    }

}