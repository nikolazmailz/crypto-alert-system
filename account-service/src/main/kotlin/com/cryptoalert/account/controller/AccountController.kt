package com.cryptoalert.account.controller

import com.cryptoalert.account.api.AccountsApi
import com.cryptoalert.account.application.AuthService
import com.cryptoalert.account.dto.UserResponse
import com.cryptoalert.shared.security.currentUserId
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Контроллер профиля текущего пользователя.
 *
 * Использует утилиту currentUserId() из shared-kernel для извлечения
 * userId из ReactiveSecurityContextHolder — без дублирования кода.
 */
@RestController
class AccountController(
    private val authService: AuthService,
) : AccountsApi {

    override suspend fun getMe(): ResponseEntity<UserResponse> {
        val userId = currentUserId()
        val user = authService.getMe(userId)
        return ResponseEntity.ok(user.toUserResponse())
    }
}
