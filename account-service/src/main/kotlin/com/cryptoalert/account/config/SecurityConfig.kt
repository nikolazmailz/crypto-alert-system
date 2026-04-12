package com.cryptoalert.account.config

import com.cryptoalert.shared.security.JwtReactiveAuthenticationManager
import com.cryptoalert.shared.security.JwtSecurityContextRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain

/**
 * Конфигурация маршрутов безопасности для account-service.
 *
 * JWT-бины (JwtReactiveAuthenticationManager, JwtSecurityContextRepository)
 * предоставляет JwtSecurityAutoConfiguration из shared-kernel автоматически
 * при наличии spring-security на classpath и jwt.secret в конфиге.
 *
 * Этот класс отвечает ТОЛЬКО за правила маршрутов — что открыто, что закрыто.
 */
@Configuration
@EnableWebFluxSecurity
class SecurityConfig(
    private val authenticationManager: JwtReactiveAuthenticationManager,
    private val securityContextRepository: JwtSecurityContextRepository,
) {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .authenticationManager(authenticationManager)
            .securityContextRepository(securityContextRepository)
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                    .pathMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                    .pathMatchers("/actuator/health").permitAll()
                    .anyExchange().authenticated()
            }
            .build()

    @Bean
    fun passwordEncoder(): BCryptPasswordEncoder = BCryptPasswordEncoder(BCRYPT_STRENGTH)

    companion object {
        private const val BCRYPT_STRENGTH = 12
    }
}
