package com.cryptoalert.portfolio.domain

import java.math.BigDecimal

data class PortfolioAsset(
    val symbol: String,
    var quantity: BigDecimal
)
