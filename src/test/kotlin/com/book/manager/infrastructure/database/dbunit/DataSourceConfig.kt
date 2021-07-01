package com.book.manager.infrastructure.database.dbunit

import com.github.springtestdbunit.bean.DatabaseConfigBean
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import javax.sql.DataSource

@TestConfiguration
class DataSourceConfig {

    @Bean
    fun dbUnitDatabaseConfig(): DatabaseConfigBean = DatabaseConfigBean().apply {
        allowEmptyFields = true
        datatypeFactory = PostgresqlDataTypeFactory()
    }

    @Bean
    fun dbUnitDatabaseConnection(dbUnitDatabaseConfig: DatabaseConfigBean, dataSource: DataSource) =
        DatabaseDataSourceConnectionFactoryBean(dataSource).apply { setDatabaseConfig(dbUnitDatabaseConfig) }
}