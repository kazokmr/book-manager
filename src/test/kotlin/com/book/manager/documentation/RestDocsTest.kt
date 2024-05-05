package com.book.manager.documentation

//import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document
import com.book.manager.infrastructure.database.testcontainers.DbTestContainerConfiguration
import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper.document
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.testcontainers.context.ImportTestcontainers
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@ImportTestcontainers(value = [DbTestContainerConfiguration::class])
@Disabled
internal class RestDocsTest(
    @Autowired private val mockMvc: MockMvc
) {

    @Test
    @DisplayName("RestDocsを作る")
    @Disabled
    fun makeTestDoc() {
        mockMvc.perform(get("/book/list"))
            .andExpect(status().isOk)
//            .andDo(document("bookList"))
            .andDo(document("bookList"))
    }
}
