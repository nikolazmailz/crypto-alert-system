package com.cryptoalert.marketdata.domain

import java.time.OffsetDateTime

/**
 * Торговая пара, синхронизированная из Binance Exchange Info.
 * Является чистой domain-моделью без зависимостей на фреймворки.
 */
data class Symbol(
    val symbol: String,
    val baseAsset: String,
    val quoteAsset: String,
    val status: String,
    val lastUpdatedAt: OffsetDateTime,
)
