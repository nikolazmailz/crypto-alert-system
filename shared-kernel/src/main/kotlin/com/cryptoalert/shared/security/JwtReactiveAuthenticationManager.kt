package com.cryptoalert.shared.security

import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import reactor.core.publisher.Mono

/**
 * Реактивный AuthenticationManager для JWT.
 * НЕ @Component — создаётся только через JwtSecurityAutoConfiguration.
 *
 * Принимает Authentication с токеном в credentials,
 * валидирует его и возвращает аутентифицированный объект.
 * principal = userId.toString() → доступен через currentUserId().
 */
class JwtReactiveAuthenticationManager(
    private val tokenService: JwtTokenService,
) : ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        val token = authentication.credentials as? String
            ?: return Mono.empty()

        if (!tokenService.isValid(token)) return Mono.empty()

        val userId = tokenService.extractUserId(token)
            ?: return Mono.empty()

        return Mono.just(
            UsernamePasswordAuthenticationToken(userId.toString(), token, emptyList()),
        )
    }
}
