package com.cryptoalert.alert

import com.cryptoalert.BaseIntegrationTest
import com.cryptoalert.alert.domain.AlertCondition
import kotlinx.coroutines.reactive.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.ActiveProfiles
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
abstract class AlertsBaseIntegrationTest : BaseIntegrationTest() {

    @Autowired
    lateinit var databaseClient: DatabaseClient

    suspend fun deleteAllAlerts() {
        databaseClient.sql("DELETE FROM alerts")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    suspend fun insertAlert(
        id: UUID,
        userId: UUID,
        symbol: String,
        targetPrice: BigDecimal,
        condition: AlertCondition,
    ) {
        databaseClient.sql(
            """
            INSERT INTO alerts (id, user_id, symbol, target_price, condition, is_active, created_at)
            VALUES (:id, :userId, :symbol, :targetPrice, :condition, :isActive, :createdAt)
            """.trimIndent(),
        )
            .bind("id", id)
            .bind("userId", userId)
            .bind("symbol", symbol)
            .bind("targetPrice", targetPrice)
            .bind("condition", condition.name)
            .bind("isActive", true)
            .bind("createdAt", Instant.now())
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    suspend fun fetchIsActive(id: UUID): Boolean? =
        databaseClient.sql("SELECT is_active FROM alerts WHERE id = :id")
            .bind("id", id)
            .fetch()
            .one()
            .map { it["is_active"] as Boolean }
            .awaitSingleOrNull()
}
