package com.cryptoalert.account.application

import com.cryptoalert.account.application.exception.EmailAlreadyExistsException
import com.cryptoalert.account.application.exception.InvalidCredentialsException
import com.cryptoalert.account.application.exception.InvalidRegistrationException
import com.cryptoalert.account.application.exception.UserNotFoundException
import com.cryptoalert.account.domain.PasswordHasher
import com.cryptoalert.account.domain.User
import com.cryptoalert.account.domain.UserRepository
import com.cryptoalert.shared.security.JwtTokenService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.util.UUID

class AuthServiceTest : ShouldSpec({

    val userRepository = mockk<UserRepository>()
    val passwordHasher = mockk<PasswordHasher>()
    val tokenService = mockk<JwtTokenService>()

    val service = AuthService(userRepository, passwordHasher, tokenService)

    beforeTest {
        clearAllMocks()
    }

    context("register") {
        should("hash password and save user on valid input") {
            val email = "new@example.com"
            val rawPassword = "SecurePass1"
            val hashed = "hashed_password"

            coEvery { userRepository.existsByEmail(email) } returns false
            coEvery { passwordHasher.hash(rawPassword) } returns hashed
            coEvery { userRepository.save(any()) } answers { firstArg() }

            val user = service.register(email, rawPassword)

            user.email shouldBe email
            user.passwordHash shouldBe hashed

            coVerify(exactly = 1) { userRepository.save(any()) }
        }

        should("throw EmailAlreadyExistsException when email is taken") {
            coEvery { userRepository.existsByEmail(any()) } returns true

            shouldThrow<EmailAlreadyExistsException> {
                service.register("taken@example.com", "SecurePass1")
            }

            coVerify(exactly = 0) { userRepository.save(any()) }
        }

        should("throw InvalidRegistrationException on invalid email") {
            coEvery { userRepository.existsByEmail(any()) } returns false

            shouldThrow<InvalidRegistrationException> {
                service.register("not-an-email", "SecurePass1")
            }
        }

        should("throw InvalidRegistrationException when password is too short") {
            coEvery { userRepository.existsByEmail(any()) } returns false

            shouldThrow<InvalidRegistrationException> {
                service.register("valid@example.com", "sho")
            }
        }
    }

    context("login") {
        should("return LoginResult with token on valid credentials") {
            val userId = UUID.randomUUID()
            val user = User(id = userId, email = "user@example.com", passwordHash = "hashed")
            val expectedToken = "jwt.token.here"

            coEvery { userRepository.findByEmail("user@example.com") } returns user
            coEvery { passwordHasher.matches("ValidPass1", "hashed") } returns true
            coEvery { tokenService.generateToken(userId) } returns expectedToken
            coEvery { tokenService.expirationMs } returns 3_600_000L

            val result = service.login("user@example.com", "ValidPass1")

            result.token shouldBe expectedToken
            result.expiresIn shouldBe 3_600_000L
        }

        should("throw InvalidCredentialsException when user not found") {
            coEvery { userRepository.findByEmail(any()) } returns null

            shouldThrow<InvalidCredentialsException> {
                service.login("ghost@example.com", "AnyPass1")
            }
        }

        should("throw InvalidCredentialsException when password is wrong") {
            val user = User(email = "user@example.com", passwordHash = "hashed")

            coEvery { userRepository.findByEmail("user@example.com") } returns user
            coEvery { passwordHasher.matches("WrongPass", "hashed") } returns false

            shouldThrow<InvalidCredentialsException> {
                service.login("user@example.com", "WrongPass")
            }
        }
    }

    context("getMe") {
        should("return user when found") {
            val userId = UUID.randomUUID()
            val user = User(id = userId, email = "me@example.com", passwordHash = "hash")

            coEvery { userRepository.findById(userId) } returns user

            val result = service.getMe(userId)

            result.id shouldBe userId
            result.email shouldBe "me@example.com"
        }

        should("throw UserNotFoundException when user does not exist") {
            val id = UUID.randomUUID()
            coEvery { userRepository.findById(id) } returns null

            shouldThrow<UserNotFoundException> {
                service.getMe(id)
            }
        }
    }
})
