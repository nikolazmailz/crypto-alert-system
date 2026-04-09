package com.cryptoalert.alert.domain

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class Alert(
    val id: UUID = UUID.randomUUID(),
    val userId: UUID,
    val symbol: String,
    val targetPrice: BigDecimal,
    val condition: AlertCondition,
    val isActive: Boolean = true,
    val createdAt: Instant = Instant.now(),
)

