package com.cryptoalert.alert.infrastructure.db

import com.cryptoalert.alert.domain.Alert
import com.cryptoalert.alert.domain.AlertRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import io.r2dbc.postgresql.codec.Json
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.UUID

@Repository
class AlertRepositoryImpl(
    private val databaseClient: DatabaseClient,
    private val objectMapper: ObjectMapper
) : AlertRepository {

    override suspend fun save(alert: Alert): UUID {
        return databaseClient.sql("""
            INSERT INTO alerts (id, user_id, symbol, target_price, condition, is_active, created_at)
            VALUES (:id, :userId, :symbol, :targetPrice, :condition, :isActive, :createdAt)
            RETURNING id
        """)
            .bind("id", alert.id)
            .bind("userId", alert.userId)
            .bind("symbol", alert.symbol)
            .bind("targetPrice", alert.targetPrice)
            .bind("condition", alert.condition.name)
            .bind("isActive", alert.isActive)
            .bind("createdAt", alert.createdAt)
            .fetch()
            .one()
            .map { it["id"] as UUID }
            .awaitSingle()
    }

    override suspend fun findTriggeredAlerts(symbol: String, currentPrice: BigDecimal): List<Alert> {
        return databaseClient.sql("""
            select jsonb_build_object(
                'id', id,
                'userId', user_id,
                'symbol', symbol,
                'targetPrice', target_price,
                'condition', condition,
                'isActive', is_active,
                'createdAt', created_at,
            ) as data
            FROM alerts
            WHERE symbol = :symbol
              AND is_active = true
              AND (
                (condition = 'GREATER_THAN' AND :currentPrice >= target_price)
                OR
                (condition = 'LESS_THAN' AND :currentPrice <= target_price)
              )
        """)
            .bind("symbol", symbol)
            .bind("currentPrice", currentPrice)
            .fetch()
            .all()
            .map {
                objectMapper.readValue<Alert>((it["data"] as Json).asString())
            }
            .collectList()
            .awaitSingleOrNull() ?: emptyList()
    }

    override suspend fun markAsSent(id: UUID) {
        databaseClient.sql("UPDATE alerts SET is_active = false WHERE id = :id")
            .bind("id", id)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
