package com.cryptoalert.marketdata.controller

import com.cryptoalert.marketdata.api.PricesApi
import com.cryptoalert.marketdata.dto.PriceResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime
import java.time.ZoneOffset

@RestController
class PricesController : PricesApi {

    // override suspend fun — корутины поддерживаются из коробки благодаря настройкам генератора
    override suspend fun getPriceBySymbol(symbol: String): ResponseEntity<PriceResponse> {

        // TODO: Здесь позже будет вызов слоя Application (например, MarketDataService)
        // Пока возвращаем хардкод-заглушку, чтобы убедиться, что API работает

        val response = PriceResponse(
            symbol = symbol.uppercase(),
            price = 50230.45,
            updatedAt = OffsetDateTime.now(ZoneOffset.UTC)
        )

        return ResponseEntity.ok(response)
    }
}
