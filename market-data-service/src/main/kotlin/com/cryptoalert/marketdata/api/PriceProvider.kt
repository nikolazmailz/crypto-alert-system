package com.cryptoalert.marketdata.api

import java.math.BigDecimal

/**
 * Публичный интерфейс для других модулей системы.
 * Позволяет получить актуальную цену без знания о внутренностях market-data.
 */
interface PriceProvider {
    suspend fun getCurrentPrice(symbol: String): BigDecimal
}
