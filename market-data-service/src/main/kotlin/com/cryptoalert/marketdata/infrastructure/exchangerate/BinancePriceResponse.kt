package com.cryptoalert.marketdata.infrastructure.exchangerate

import java.math.BigDecimal

data class BinancePriceResponse(
    val symbol: String,
    val price: BigDecimal
)
