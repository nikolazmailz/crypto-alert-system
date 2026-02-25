package com.cryptoalert.marketdata.domain

import java.math.BigDecimal
import java.time.OffsetDateTime

data class CryptoPrice(
    val symbol: String,
    val price: BigDecimal,
    val updatedAt: OffsetDateTime
)
