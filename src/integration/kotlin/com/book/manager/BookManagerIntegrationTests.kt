package com.book.manager

import com.book.manager.application.service.mockuser.WithCustomMockUser
import com.book.manager.presentation.form.BookInfo
import com.book.manager.presentation.form.GetBookListResponse
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.nio.charset.StandardCharsets

@SpringBootTest
@AutoConfigureMockMvc
internal class BookManagerIntegrationTests(@Autowired val mockMvc: MockMvc) {

    @Test
    @DisplayName("書籍リストを取得する")
    @WithCustomMockUser
    fun `bookList when list is exist then return them`() {

        // Given
        val expected = GetBookListResponse(
            listOf(
                BookInfo(100, "Kotlin入門", "コトリン太郎", false),
                BookInfo(200, "Java入門", "ジャヴァ太郎", false),
                BookInfo(400, "Kotlinサーバーサイドプログラミング実践", "****", true),
                BookInfo(500, "タイトル", "オーサー", false)
            )
        )

        // When
        val resultResponse =
            mockMvc
                .perform(
                    get("/book/list")
                        .with(csrf().asHeader())
                )
                .andExpect(status().isOk)
                .andReturn()
                .response

        // Then
        val result = resultResponse.getContentAsString(StandardCharsets.UTF_8)
            .let {
                ObjectMapper()
                    .registerKotlinModule()
                    .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
                    .readValue(it, GetBookListResponse::class.java)
            }
        assertThat(result.bookList).containsAll(expected.bookList)
    }

    @Test
    @DisplayName("ログインテスト")
    fun `login when user is exist then login`() {
        // Given
        val user = "admin@example.com"
        val pass = "admin"

        // When
        // Then
        mockMvc
            .perform(
                formLogin()
                    .loginProcessingUrl("/login")
                    .user("email", user)
                    .password("pass", pass)
            )
            .andExpect(status().isOk)
    }

}
