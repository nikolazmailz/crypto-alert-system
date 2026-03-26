package com.cryptoalert.shared.error

import org.springframework.http.HttpStatus

class ResourceNotFoundException(message: String) : DomainException(
    message = message,
    status = HttpStatus.NOT_FOUND,
    type = "https://crypto-alert-system.com/errors/not-found",
)
