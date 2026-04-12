package com.cryptoalert.account.infrastructure.security

import com.cryptoalert.shared.security.JwtProperties
import com.cryptoalert.shared.security.JwtSecurityService
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.UUID

class JwtServiceTest : ShouldSpec({

    val config = JwtProperties(
        secret = "test-secret-key-for-unit-tests-must-be-at-least-32-chars",
        expirationMs = 3_600_000L,
    )
    val jwtSecurityService = JwtSecurityService(config)

    context("generateToken") {
        should("generate a non-empty token string") {
            val token = jwtSecurityService.generateToken(UUID.randomUUID())
            token shouldNotBe null
            token.isNotBlank().shouldBeTrue()
        }

        should("generate different tokens for different users") {
            val token1 = jwtSecurityService.generateToken(UUID.randomUUID())
            val token2 = jwtSecurityService.generateToken(UUID.randomUUID())
            (token1 != token2).shouldBeTrue()
        }
    }

    context("extractUserId") {
        should("extract the same userId that was used to generate the token") {
            val userId = UUID.randomUUID()
            val token = jwtSecurityService.generateToken(userId)
            val extracted = jwtSecurityService.extractUserId(token)
            extracted shouldBe userId
        }

        should("return null for an invalid token") {
            val result = jwtSecurityService.extractUserId("not.a.valid.token")
            result shouldBe null
        }

        should("return null for an empty string") {
            val result = jwtSecurityService.extractUserId("")
            result shouldBe null
        }
    }

    context("isValid") {
        should("return true for a freshly generated token") {
            val token = jwtSecurityService.generateToken(UUID.randomUUID())
            jwtSecurityService.isValid(token).shouldBeTrue()
        }

        should("return false for a tampered token") {
            val token = jwtSecurityService.generateToken(UUID.randomUUID())
            val tampered = token.dropLast(5) + "xxxxx"
            jwtSecurityService.isValid(tampered).shouldBeFalse()
        }

        should("return false for a token signed with a different key") {
            val otherConfig = JwtProperties(
                secret = "completely-different-secret-key-also-at-least-32-chars",
                expirationMs = 3_600_000L,
            )
            val otherService = JwtSecurityService(otherConfig)
            val token = otherService.generateToken(UUID.randomUUID())
            jwtSecurityService.isValid(token).shouldBeFalse()
        }

        should("return false for an expired token") {
            val expiredConfig = JwtProperties(
                secret = "test-secret-key-for-unit-tests-must-be-at-least-32-chars",
                expirationMs = -1L, // уже истёк
            )
            val expiredService = JwtSecurityService(expiredConfig)
            val token = expiredService.generateToken(UUID.randomUUID())
            jwtSecurityService.isValid(token).shouldBeFalse()
        }
    }
})
