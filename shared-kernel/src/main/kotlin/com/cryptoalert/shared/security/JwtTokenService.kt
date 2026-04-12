package com.cryptoalert.shared.security

import java.util.UUID

/**
 * Порт для работы с JWT-токенами. Живёт в shared-kernel,
 * чтобы быть доступным во всех модулях через инъекцию.
 *
 * account-service: использует generateToken() при логине.
 * alert-service / portfolio-service: только isValid() + extractUserId()
 *   через JwtReactiveAuthenticationManager (автоматически).
 */
interface JwtTokenService {
    fun generateToken(userId: UUID): String
    fun extractUserId(token: String): UUID?
    fun isValid(token: String): Boolean
    val expirationMs: Long
}
