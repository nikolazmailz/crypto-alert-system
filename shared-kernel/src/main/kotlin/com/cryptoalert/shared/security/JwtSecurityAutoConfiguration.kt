package com.cryptoalert.shared.security

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.security.config.web.server.ServerHttpSecurity

/**
 * Spring Boot Auto-Configuration для JWT-безопасности.
 *
 * Активируется ТОЛЬКО при одновременном выполнении двух условий:
 *   1. spring-security на classpath (@ConditionalOnClass)
 *   2. jwt.secret задан в application.yml (@ConditionalOnProperty)
 *
 * Это означает:
 *   - market-data-service без spring-security → бины НЕ создаются
 *   - alert-service с spring-security, но без jwt.secret → бины НЕ создаются
 *   - account-service с обоими → бины создаются автоматически ✓
 *
 * Регистрация: META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
 *
 * Добавить JWT-безопасность в любой сервис — три шага:
 *   1. implementation(libs.spring.boot.starter.security) в build.gradle.kts
 *   2. implementation/runtimeOnly jjwt-api/impl/jackson
 *   3. jwt.secret в application.yml (тот же секрет, что у account-service)
 *   4. Определить свой SecurityWebFilterChain с нужными правилами маршрутов
 */
@AutoConfiguration
@ConditionalOnClass(ServerHttpSecurity::class)
@ConditionalOnProperty(prefix = "jwt", name = ["secret"])
@EnableConfigurationProperties(JwtProperties::class)
class JwtSecurityAutoConfiguration(
    private val jwtProperties: JwtProperties,
) {

    @Bean
    @ConditionalOnMissingBean
    fun jwtTokenService(): JwtSecurityService =
        JwtSecurityService(jwtProperties)

    @Bean
    @ConditionalOnMissingBean
    fun jwtReactiveAuthenticationManager(
        tokenService: JwtTokenService,
    ): JwtReactiveAuthenticationManager =
        JwtReactiveAuthenticationManager(tokenService)

    @Bean
    @ConditionalOnMissingBean
    fun jwtSecurityContextRepository(
        authManager: JwtReactiveAuthenticationManager,
    ): JwtSecurityContextRepository =
        JwtSecurityContextRepository(authManager)
}
