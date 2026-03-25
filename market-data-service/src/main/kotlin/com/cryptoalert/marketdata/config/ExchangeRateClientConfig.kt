package com.cryptoalert.marketdata.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class ExchangeRateClientConfig(
    @Value("\${binance.api.url}")
    private val binanceApiUrl: String
) {

    @Bean
    fun binanceWebClient(builder: WebClient.Builder): WebClient {
        return builder
            .baseUrl(binanceApiUrl)
            .build()
    }
}
