package com.cryptoalert.portfolio

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.cryptoalert.shared", "com.cryptoalert.portfolio"])
class PortfolioApplication

fun main(args: Array<String>) {
    runApplication<PortfolioApplication>(*args)
}
