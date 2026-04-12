package com.cryptoalert.account.controller

import com.cryptoalert.account.api.AuthApi
import com.cryptoalert.account.application.AuthService
import com.cryptoalert.account.dto.LoginRequest
import com.cryptoalert.account.dto.RegisterRequest
import com.cryptoalert.account.dto.TokenResponse
import com.cryptoalert.account.dto.UserResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.net.URI

/**
 * Тонкий контроллер аутентификации.
 * Делегирует всю логику в AuthService.
 * Не содержит бизнес-правил.
 */
@RestController
class AuthController(
    private val authService: AuthService,
) : AuthApi {

    override suspend fun register(registerRequest: RegisterRequest): ResponseEntity<UserResponse> {
        val user = authService.register(
            email = registerRequest.email,
            password = registerRequest.password,
        )
        return ResponseEntity
            .created(URI.create("/api/v1/accounts/me"))
            .body(user.toUserResponse())
    }

    override suspend fun login(loginRequest: LoginRequest): ResponseEntity<TokenResponse> {
        val result = authService.login(
            email = loginRequest.email,
            password = loginRequest.password,
        )
        return ResponseEntity.ok(result.toTokenResponse())
    }
}
