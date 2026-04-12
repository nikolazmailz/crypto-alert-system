package com.cryptoalert.shared.security

import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import java.util.UUID

/**
 * Извлекает ID текущего аутентифицированного пользователя из
 * ReactiveSecurityContextHolder.
 *
 * Должна вызываться внутри suspend-функции (корутины).
 * Authentication.name содержит userId.toString(), который устанавливается
 * в JwtAuthenticationManager при валидации JWT-токена.
 *
 * Использование в любом модуле (alert, portfolio и т.д.):
 * ```kotlin
 * suspend fun createAlert(...) {
 *     val userId = currentUserId()
 *     alertService.createAlert(userId, ...)
 * }
 * ```
 *
 * @throws NoSuchElementException если SecurityContext пуст
 *   (запрос не прошёл через JwtServerSecurityContextRepository)
 * @throws IllegalArgumentException если subject токена не является валидным UUID
 */
suspend fun currentUserId(): UUID {
    val context = ReactiveSecurityContextHolder.getContext().awaitSingle()
    return UUID.fromString(context.authentication.name)
}
