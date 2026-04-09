package com.cryptoalert.alert

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.cryptoalert.shared", "com.cryptoalert.alert"])
class AlertApplication

fun main(args: Array<String>) {
    runApplication<AlertApplication>(*args)
}
