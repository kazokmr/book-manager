package com.book.manager.presentation.config

import io.micrometer.observation.Observation
import io.micrometer.observation.ObservationRegistry
import org.apache.ibatis.cache.CacheKey
import org.apache.ibatis.executor.Executor
import org.apache.ibatis.mapping.BoundSql
import org.apache.ibatis.mapping.MappedStatement
import org.apache.ibatis.plugin.Interceptor
import org.apache.ibatis.plugin.Intercepts
import org.apache.ibatis.plugin.Invocation
import org.apache.ibatis.plugin.Signature
import org.apache.ibatis.session.ResultHandler
import org.apache.ibatis.session.RowBounds
import org.springframework.stereotype.Component

@Intercepts(
    Signature(
        type = Executor::class,
        method = "update",
        args = [MappedStatement::class, Any::class]
    ),
    Signature(
        type = Executor::class,
        method = "query",
        args = [MappedStatement::class, Any::class, RowBounds::class, ResultHandler::class, CacheKey::class, BoundSql::class]
    ),
    Signature(
        type = Executor::class,
        method = "query",
        args = [MappedStatement::class, Any::class, RowBounds::class, ResultHandler::class]
    )
)
@Component
class MicrometerInterceptor(private val observationRegistry: ObservationRegistry) : Interceptor {
    override fun intercept(invocation: Invocation): Any {
        val mappedStatement = invocation.args[0] as MappedStatement
        return Observation.createNotStarted("mybatis.query", observationRegistry)
            .lowCardinalityKeyValue("id", mappedStatement.id)
            .observe(invocation::proceed)
    }
}
