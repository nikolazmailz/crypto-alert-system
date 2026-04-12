package com.cryptoalert.alert.config

import com.cryptoalert.shared.security.JwtReactiveAuthenticationManager
import com.cryptoalert.shared.security.JwtSecurityContextRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

/**
 * JWT-безопасность для alert-service.
 *
 * JWT-бины (JwtReactiveAuthenticationManager, JwtSecurityContextRepository)
 * предоставляет JwtSecurityAutoConfiguration из shared-kernel автоматически —
 * писать их заново не нужно.
 *
 * Этот класс отвечает только за правила маршрутов этого сервиса.
 * currentUserId() в AlertController/AlertService работает автоматически.
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
                    .pathMatchers("/actuator/health").permitAll()
                    .anyExchange().authenticated() // все alert-эндпоинты требуют JWT
            }
            .build()
}
