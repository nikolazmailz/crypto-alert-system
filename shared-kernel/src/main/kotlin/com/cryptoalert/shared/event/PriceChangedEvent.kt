package com.cryptoalert.shared.event

import java.math.BigDecimal
import java.time.Instant

/**
 * Неизменяемое событие изменения цены.
 * Используем стандартный ApplicationEvent от Spring или просто POJO.
 */
data class PriceChangedEvent(
    val symbol: String,
    val price: BigDecimal,
    val timestamp: Instant = Instant.now()
)
