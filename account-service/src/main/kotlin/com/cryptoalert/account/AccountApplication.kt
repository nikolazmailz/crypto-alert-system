package com.cryptoalert.account

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * JWT-бины (JwtSecurityService, JwtReactiveAuthenticationManager, JwtSecurityContextRepository)
 * создаются автоматически через JwtSecurityAutoConfiguration из shared-kernel
 * при наличии spring-security на classpath и jwt.secret в application.yml.
 *
 * JwtProperties биндится через @EnableConfigurationProperties
 * внутри самой JwtSecurityAutoConfiguration — явно здесь не нужно.
 */
@SpringBootApplication(scanBasePackages = ["com.cryptoalert.shared", "com.cryptoalert.account"])
class AccountApplication

fun main(args: Array<String>) {
    runApplication<AccountApplication>(*args)
}
