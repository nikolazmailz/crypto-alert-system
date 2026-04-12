package com.cryptoalert.account.infrastructure.security

import com.cryptoalert.account.domain.PasswordHasher
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

/**
 * Реализует доменный порт PasswordHasher через BCrypt.
 * Сила хеширования: 12 (соответствует рекомендациям OWASP).
 */
@Component
class BCryptPasswordHasher(
    private val passwordEncoder: BCryptPasswordEncoder,
) : PasswordHasher {

    override fun hash(rawPassword: String): String =
        passwordEncoder.encode(rawPassword)

    override fun matches(rawPassword: String, encodedPassword: String): Boolean =
        passwordEncoder.matches(rawPassword, encodedPassword)
}
