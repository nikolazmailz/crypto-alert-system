package com.cryptoalert.account.application.dto

data class LoginResult(
    val token: String,
    val expiresIn: Long,
)
