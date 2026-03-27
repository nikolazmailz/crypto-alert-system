package com.cryptoalert.portfolio.controller

import com.cryptoalert.portfolio.application.PortfolioService
import com.cryptoalert.portfolio.controller.dto.AddAssetRequest
import com.cryptoalert.portfolio.controller.dto.PortfolioValueResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
class PortfolioController(
    private val portfolioService: PortfolioService
) : PortfolioApi {

    override suspend fun addAsset(userId: UUID, addAssetRequest: AddAssetRequest): ResponseEntity<Unit> {
        portfolioService.addAsset(
            userId = userId,
            symbol = addAssetRequest.symbol,
            quantity = addAssetRequest.quantity.toBigDecimal()
        )
        return ResponseEntity.ok().build()
    }

    override suspend fun getTotalValue(userId: UUID): ResponseEntity<PortfolioValueResponse> {
        val totalValue = portfolioService.calculateTotalValue(userId)

        return ResponseEntity.ok(
            PortfolioValueResponse(
                userId = userId,
                totalValue = totalValue.toDouble()
            )
        )
    }
}
