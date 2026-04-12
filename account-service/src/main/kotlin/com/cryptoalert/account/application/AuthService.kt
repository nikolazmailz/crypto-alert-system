package com.cryptoalert.account.application

import com.cryptoalert.account.application.dto.LoginResult
import com.cryptoalert.account.application.exception.EmailAlreadyExistsException
import com.cryptoalert.account.application.exception.InvalidCredentialsException
import com.cryptoalert.account.application.exception.InvalidRegistrationException
import com.cryptoalert.account.application.exception.UserNotFoundException
import com.cryptoalert.account.domain.PasswordHasher
import com.cryptoalert.account.domain.User
import com.cryptoalert.account.domain.UserRepository
import com.cryptoalert.shared.security.JwtTokenService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordHasher: PasswordHasher,
    private val jwtTokenService: JwtTokenService,
) {
    private val log = KotlinLogging.logger {}

    suspend fun register(email: String, password: String): User {
        runCatching { User.validateEmail(email) }
            .onFailure { throw InvalidRegistrationException(it.message ?: "Invalid email") }

        runCatching { User.validatePassword(password) }
            .onFailure { throw InvalidRegistrationException(it.message ?: "Weak password") }

        if (userRepository.existsByEmail(email)) {
            throw EmailAlreadyExistsException(email)
        }

        val user = User(
            email = email.lowercase().trim(),
            passwordHash = passwordHasher.hash(password),
        )

        return userRepository.save(user).also {
            log.info { "Registered new user id=${it.id}" }
        }
    }

    suspend fun login(email: String, password: String): LoginResult {
        val user = userRepository.findByEmail(email.lowercase().trim())
            ?: throw InvalidCredentialsException()

        if (!passwordHasher.matches(password, user.passwordHash)) {
            throw InvalidCredentialsException()
        }

        val token = jwtTokenService.generateToken(user.id)
        log.info { "User id=${user.id} authenticated successfully" }

        return LoginResult(token = token, expiresIn = jwtTokenService.expirationMs)
    }

    suspend fun getMe(userId: UUID): User =
        userRepository.findById(userId) ?: throw UserNotFoundException(userId)
}
