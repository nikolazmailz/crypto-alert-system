package com.cryptoalert.account.application.exception

import com.cryptoalert.shared.error.DomainException
import org.springframework.http.HttpStatus
import java.util.UUID

class EmailAlreadyExistsException(email: String) : DomainException(
    message = "User with email '$email' already exists",
    status = HttpStatus.CONFLICT,
    type = "https://crypto-alert-system.com/errors/email-already-exists",
)

class InvalidCredentialsException : DomainException(
    message = "Invalid email or password",
    status = HttpStatus.UNAUTHORIZED,
    type = "https://crypto-alert-system.com/errors/invalid-credentials",
)

class UserNotFoundException(id: UUID) : DomainException(
    message = "User not found: $id",
    status = HttpStatus.NOT_FOUND,
    type = "https://crypto-alert-system.com/errors/user-not-found",
)

class InvalidRegistrationException(reason: String) : DomainException(
    message = reason,
    status = HttpStatus.UNPROCESSABLE_ENTITY,
    type = "https://crypto-alert-system.com/errors/invalid-registration",
)
