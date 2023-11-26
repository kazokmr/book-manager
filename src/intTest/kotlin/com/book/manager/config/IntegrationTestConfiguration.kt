package com.book.manager.config

import com.book.manager.infrastructure.database.mapper.AccountMapper
import com.book.manager.infrastructure.database.mapper.BookMapper
import com.book.manager.infrastructure.database.mapper.RentalMapper
import com.book.manager.infrastructure.database.testcontainers.TestContainerDataRegistry
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.testcontainers.context.ImportTestcontainers
import org.springframework.context.annotation.Bean

@TestConfiguration
@ImportTestcontainers(TestContainerDataRegistry::class) // From Spring Boot 3.1 intTestはDBが常時必要なのでConfigurationでTestContainerを起動させる
class IntegrationTestConfiguration {

    @Bean
    fun exchangeFilter(): CustomExchangeFilterFunction = CustomExchangeFilterFunction()

    @Bean
    fun testMapper(accountMapper: AccountMapper, bookMapper: BookMapper, rentalMapper: RentalMapper): CustomTestMapper =
        CustomTestMapper(accountMapper, bookMapper, rentalMapper)

    @Bean
    fun jsonConverter(): CustomJsonConverter = CustomJsonConverter()
}
