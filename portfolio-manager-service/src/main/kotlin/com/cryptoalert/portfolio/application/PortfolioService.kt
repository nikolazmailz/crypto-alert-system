package com.cryptoalert.portfolio.application

import com.cryptoalert.marketdata.api.PriceProvider
import com.cryptoalert.portfolio.domain.Portfolio
import com.cryptoalert.portfolio.domain.PortfolioRepository
import com.cryptoalert.shared.cached.CaffeineStampedeCache
import com.cryptoalert.shared.error.ResourceNotFoundException
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID

@Service
class PortfolioService(
    private val portfolioRepository: PortfolioRepository,
    private val priceProvider: PriceProvider,
    private val priceCache: CaffeineStampedeCache<String, BigDecimal>,
) {

    suspend fun addAsset(userId: UUID, symbol: String, quantity: BigDecimal) {
        val portfolio = portfolioRepository.findByUserId(userId)
            ?: Portfolio(userId = userId)

        portfolio.addAsset(symbol, quantity)
        portfolioRepository.save(portfolio)
    }

    suspend fun calculateTotalValue(userId: UUID): BigDecimal {
        val portfolio = portfolioRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Portfolio not found")

        return portfolio.assets.sumOf { asset ->
            val currentPrice =
                priceCache.get(asset.symbol) {
                    priceProvider.getCurrentPrice(asset.symbol)
                }

            asset.quantity.multiply(currentPrice)
        }
    }
}
