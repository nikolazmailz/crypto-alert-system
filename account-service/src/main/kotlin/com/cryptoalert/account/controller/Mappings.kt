package com.cryptoalert.account.controller

import com.cryptoalert.account.application.dto.LoginResult
import com.cryptoalert.account.domain.User
import com.cryptoalert.account.dto.TokenResponse
import com.cryptoalert.account.dto.UserResponse

// ─── Domain → DTO (controller layer, одностороннее преобразование) ───────────

fun User.toUserResponse(): UserResponse =
    UserResponse(
        id = id,
        email = email,
        createdAt = createdAt,
    )

fun LoginResult.toTokenResponse(): TokenResponse =
    TokenResponse(
        token = token,
        expiresIn = expiresIn,
    )
