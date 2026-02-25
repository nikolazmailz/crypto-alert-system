package com.cryptoalert.marketdata.domain

import java.math.BigDecimal

/**
 * Домен должен декларировать свои потребности.
 * */
interface ExchangeRateProvider {
    /**
     * Возвращает текущую цену для пары (например, "BTCUSDT").
     * Возвращает null, если пара не найдена или произошла ошибка.
     */
    suspend fun fetchPrice(symbol: String): BigDecimal?
}
