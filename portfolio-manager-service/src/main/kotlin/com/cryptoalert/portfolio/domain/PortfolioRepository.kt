package com.cryptoalert.portfolio.domain

import java.util.UUID

interface PortfolioRepository {
    suspend fun findByUserId(userId: UUID): Portfolio?
    suspend fun save(portfolio: Portfolio): Portfolio
}
