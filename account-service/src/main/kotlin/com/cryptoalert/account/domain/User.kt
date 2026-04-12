package com.cryptoalert.account.domain

import java.time.OffsetDateTime
import java.util.UUID

data class User(
    val id: UUID = UUID.randomUUID(),
    val email: String,
    val passwordHash: String,
    val createdAt: OffsetDateTime = OffsetDateTime.now(),
) {
    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        private const val MIN_PASSWORD_LENGTH = 4

        fun validateEmail(email: String) {
            require(EMAIL_REGEX.matches(email)) {
                "Invalid email format: $email"
            }
        }

        fun validatePassword(password: String) {
            require(password.length >= MIN_PASSWORD_LENGTH) {
                "Password must be at least $MIN_PASSWORD_LENGTH characters long"
            }
        }
    }
}
