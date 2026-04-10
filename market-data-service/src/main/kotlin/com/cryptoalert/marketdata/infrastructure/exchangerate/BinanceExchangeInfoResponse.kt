package com.cryptoalert.marketdata.infrastructure.exchangerate

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * Ответ Binance на запрос GET /api/v3/exchangeInfo.
 * Содержит сотни полей — игнорируем всё лишнее, оставляем только список пар.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class BinanceExchangeInfoResponse(
    val symbols: List<BinanceSymbolInfo>,
)

/**
 * Одна торговая пара из exchangeInfo Binance.
 * Из всех полей (statusFilter, filters, permissions и др.) берём только необходимые.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class BinanceSymbolInfo(
    val symbol: String,
    val status: String,
    val baseAsset: String,
    val quoteAsset: String,
)
