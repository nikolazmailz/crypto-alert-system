package com.cryptoalert.alert.domain

import java.math.BigDecimal
import java.util.UUID

interface AlertRepository {
    suspend fun save(alert: Alert): UUID
    suspend fun findTriggeredAlerts(symbol: String, currentPrice: BigDecimal): List<Alert>
    suspend fun markAsSent(id: UUID)
}
