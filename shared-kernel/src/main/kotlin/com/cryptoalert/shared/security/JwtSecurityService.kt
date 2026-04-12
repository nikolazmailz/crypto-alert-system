package com.cryptoalert.shared.security

import io.github.oshai.kotlinlogging.KotlinLogging
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

/**
 * Реализация JwtTokenService через JJWT 0.12.x.
 *
 * НЕ аннотирован @Component — создаётся только через JwtSecurityAutoConfiguration
 * (@ConditionalOnProperty + @ConditionalOnClass), чтобы не активироваться
 * в сервисах без явного jwt.secret в конфиге.
 */
class JwtSecurityService(private val properties: JwtProperties) : JwtTokenService {

    private val log = KotlinLogging.logger {}

    private val signingKey: SecretKey by lazy {
        Keys.hmacShaKeyFor(properties.secret.toByteArray(Charsets.UTF_8))
    }

    override val expirationMs: Long
        get() = properties.expirationMs

    override fun generateToken(userId: UUID): String {
        val now = Date()
        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(now)
            .expiration(Date(now.time + properties.expirationMs))
            .signWith(signingKey)
            .compact()
    }

    override fun extractUserId(token: String): UUID? =
        runCatching {
            val subject = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .payload
                .subject
            UUID.fromString(subject)
        }.onFailure { log.debug { "JWT extractUserId failed: ${it.message}" } }
         .getOrNull()

    override fun isValid(token: String): Boolean =
        runCatching {
            Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token)
            true
        }.onFailure { ex ->
            when (ex) {
                is JwtException -> log.debug { "Invalid JWT: ${ex.message}" }
                else -> log.warn { "Unexpected JWT error: ${ex.message}" }
            }
        }.getOrElse { false }
}
