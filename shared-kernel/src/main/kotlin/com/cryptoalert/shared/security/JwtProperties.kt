package com.cryptoalert.shared.security

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * Конфигурация JWT — одна для всех сервисов.
 *
 * Все сервисы, выпускающие или валидирующие токены,
 * должны иметь один и тот же `jwt.secret`.
 * В проде задавать через переменную окружения JWT_SECRET.
 */
@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val expirationMs: Long = 3_600_000L,
)
