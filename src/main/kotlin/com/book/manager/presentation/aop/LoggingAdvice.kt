package com.book.manager.presentation.aop

import com.book.manager.application.service.security.BookManagerUserDetails
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.AfterThrowing
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

//private val logger = LoggerFactory.getLogger(LoggingAdvice::class.java)
private val logger: Logger = LogManager.getLogger(LoggingAdvice::class.java)

@Aspect
@Component
class LoggingAdvice {

    @Before("execution(* com.book.manager.presentation.controller..*.*(..))")
    fun beforeLog(joinPoint: JoinPoint) {
        val account = SecurityContextHolder.getContext().authentication.principal as BookManagerUserDetails
        logger.info("Start: ${joinPoint.signature} accountId=${account.id}")
        logger.info("Class: ${joinPoint.target.javaClass}")
        logger.info(
            "Session: ${(RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request.session.id}"
        )
    }

    @After("execution(* com.book.manager.presentation.controller..*.*(..))")
    fun afterLog(joinPoint: JoinPoint) {
        val account = SecurityContextHolder.getContext().authentication.principal as BookManagerUserDetails
        logger.info("End: ${joinPoint.signature} accountId-${account.id}")
    }

    @Around("execution(* com.book.manager.presentation.controller..*.*(..))")
    fun aroundLog(joinPoint: ProceedingJoinPoint): Any? {
        val account = SecurityContextHolder.getContext().authentication.principal as BookManagerUserDetails
        logger.info("Start Proceed: ${joinPoint.signature} accountId=${account.id}")

        val result = joinPoint.proceed()

        logger.info("End Proceed: ${joinPoint.signature} accountId=${account.id}")

        return result
    }

    @AfterReturning("execution(* com.book.manager.presentation.controller..*.*(..))", returning = "returnValue")
    fun afterReturningLog(joinPoint: JoinPoint, returnValue: Any?) {
        logger.info("End: ${joinPoint.signature} returnValue=$returnValue")
    }

    @AfterThrowing("execution(* com.book.manager.presentation.controller..*.*(..))", throwing = "e")
    fun afterThrowingLog(joinPoint: JoinPoint, e: IllegalArgumentException) {
        logger.error("Exception: ${e.javaClass} signature=${joinPoint.signature} message=${e.message}")
    }
}