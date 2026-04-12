package com.cryptoalert.shared.security

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextImpl
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

private const val BEARER_PREFIX = "Bearer "

/**
 * Stateless ServerSecurityContextRepository для JWT.
 * НЕ @Component — создаётся только через JwtSecurityAutoConfiguration.
 *
 * При каждом запросе извлекает токен из "Authorization: Bearer <token>"
 * и делегирует валидацию в JwtReactiveAuthenticationManager.
 */
class JwtSecurityContextRepository(
    private val authenticationManager: JwtReactiveAuthenticationManager,
) : ServerSecurityContextRepository {

    override fun save(exchange: ServerWebExchange, context: SecurityContext): Mono<Void> =
        Mono.empty() // stateless — сохранение не нужно

    override fun load(exchange: ServerWebExchange): Mono<SecurityContext> {
        val token = exchange.request.headers
            .getFirst(HttpHeaders.AUTHORIZATION)
            ?.takeIf { it.startsWith(BEARER_PREFIX) }
            ?.removePrefix(BEARER_PREFIX)
            ?: return Mono.empty()

        return authenticationManager
            .authenticate(UsernamePasswordAuthenticationToken(token, token))
            .map { auth -> SecurityContextImpl(auth) as SecurityContext }
    }
}
