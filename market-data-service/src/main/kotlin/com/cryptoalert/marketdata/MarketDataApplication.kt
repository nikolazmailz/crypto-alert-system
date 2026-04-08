package com.cryptoalert.marketdata

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.cryptoalert.shared", "com.cryptoalert.marketdata"])
class MarketDataApplication

fun main(args: Array<String>) {
    runApplication<MarketDataApplication>(*args)
}
