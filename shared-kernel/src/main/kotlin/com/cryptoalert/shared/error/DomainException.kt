package com.cryptoalert.shared.error

import org.springframework.http.HttpStatus

abstract class DomainException(
    override val message: String,
    val status: HttpStatus,
    val type: String
) : RuntimeException(message)
