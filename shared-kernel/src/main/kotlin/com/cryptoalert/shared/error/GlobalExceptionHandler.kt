package com.cryptoalert.shared.error

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ServerWebExchange
import java.net.URI

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = KotlinLogging.logger {}

    @ExceptionHandler(DomainException::class)
    fun handleDomainException(ex: DomainException, exchange: ServerWebExchange): ResponseEntity<ProblemDetail> {
        log.warn { "Domain error: ${ex.message}" }
        val detail = ProblemDetail.forStatusAndDetail(ex.status, ex.message).apply {
            type = URI.create(ex.type)
            instance = URI.create(exchange.request.path.value())
        }
        return ResponseEntity.status(ex.status).body(detail)
    }

    // Перехватчик для всех остальных (непредвиденных) ошибок (500)
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception,
        exchange: ServerWebExchange,
    ): ResponseEntity<ProblemDetail> {

        log.error { "Unhandled exception: $ex" }

        val problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred",
        ).apply {
            type = URI.create("https://crypto-alert-system.com/errors/internal-server-error")
            title = "Internal Server Error"
            instance = URI.create(exchange.request.path.value())
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail)
    }
}
