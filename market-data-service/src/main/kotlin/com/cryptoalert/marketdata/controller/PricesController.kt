package com.cryptoalert.marketdata.controller

import com.cryptoalert.marketdata.api.PricesApi
import com.cryptoalert.marketdata.application.MarketDataService
import com.cryptoalert.marketdata.dto.PriceResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime
import java.time.ZoneOffset

@RestController
class PricesController(
    private val marketDataService: MarketDataService
): PricesApi {

    // override suspend fun — корутины поддерживаются из коробки благодаря настройкам генератора
    override suspend fun getPriceBySymbol(symbol: String): ResponseEntity<PriceResponse> {
        val cryptoPrice = marketDataService.getPrice(symbol)
            ?: return ResponseEntity.notFound().build() // Возвращаем 404, если цены нет

        // Маппим Domain сущность в DTO
        val response = PriceResponse(
            symbol = cryptoPrice.symbol,
            price = cryptoPrice.price.toDouble(),
            updatedAt = cryptoPrice.updatedAt
        )

        return ResponseEntity.ok(response)
    }
}

