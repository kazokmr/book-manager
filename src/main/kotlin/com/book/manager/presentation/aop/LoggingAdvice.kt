package com.book.manager.presentation.aop

import com.book.manager.application.service.security.BookManagerUserDetails
import com.book.manager.presentation.controller.CsrfTokenController
import org.aspectj.lang.JoinPoint
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.After
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.AfterThrowing
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.slf4j.LoggerFactory
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

private val logger: org.slf4j.Logger = LoggerFactory.getLogger(LoggingAdvice::class.java)
//private val logger: Logger = LogManager.getLogger(LoggingAdvice::class.java)

@Aspect
@Component
class LoggingAdvice {

    @Before("execution(* com.book.manager.presentation.controller..*.*(..))")
    fun beforeLog(joinPoint: JoinPoint) {
        // 認証前に呼ばれるコントローラは何もしない
        if (joinPoint.target.javaClass == CsrfTokenController::class.java) {
            return
        }
        val account = SecurityContextHolder.getContext().authentication.principal as BookManagerUserDetails
        logger.info("Start: ${joinPoint.signature} accountId=${account.id}")
        logger.info("Class: ${joinPoint.target.javaClass}")
        logger.info(
            "Session: ${(RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request.session.id}"
        )
    }

    @After("execution(* com.book.manager.presentation.controller..*.*(..))")
    fun afterLog(joinPoint: JoinPoint) {
        // 認証前に呼ばれるコントローラは何もしない
        if (joinPoint.target.javaClass == CsrfTokenController::class.java) {
            return
        }
        val account = SecurityContextHolder.getContext().authentication.principal as BookManagerUserDetails
        logger.info("End: ${joinPoint.signature} accountId=${account.id}")
    }

    @Around("execution(* com.book.manager.presentation.controller..*.*(..))")
    fun aroundLog(joinPoint: ProceedingJoinPoint): Any? {
        // 認証前に呼ばれるコントローラは何もしない
        if (joinPoint.target.javaClass == CsrfTokenController::class.java) {
            return joinPoint.proceed()
        }
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

    @AfterThrowing(
        "execution(* com.book.manager.application..*.*(..)) || " +
                "execution(* com.book.manager.presentation.controller..*.*(..))",
        throwing = "e"
    )
    fun afterThrowingLog(joinPoint: JoinPoint, e: Exception) {
        logger.error("Exception: ${e.javaClass} signature=${joinPoint.signature} message=${e.message}")
    }
}