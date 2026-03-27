package com.cryptoalert.portfolio.domain

import java.math.BigDecimal
import java.util.UUID

/**
 * Агрегат Портфеля.
 * Следуем принципу Rich Model: логика изменения состояния внутри сущности.
 */
data class Portfolio(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val assets: MutableList<PortfolioAsset> = mutableListOf()
) {
    fun addAsset(symbol: String, quantity: BigDecimal) {
        val existing = assets.find { it.symbol == symbol.uppercase() }
        if (existing != null) {
            existing.quantity += quantity
        } else {
            assets.add(PortfolioAsset(symbol.uppercase(), quantity))
        }
    }
}
