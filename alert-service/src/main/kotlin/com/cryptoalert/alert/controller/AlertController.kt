package com.cryptoalert.alert.controller

import com.cryptoalert.alert.api.AlertsApi
import com.cryptoalert.alert.application.AlertService
import com.cryptoalert.alert.domain.Alert
import com.cryptoalert.alert.domain.AlertCondition
import com.cryptoalert.alert.dto.AlertResponse
import com.cryptoalert.alert.dto.CreateAlertRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal
import java.time.ZoneOffset
import java.util.UUID

@RestController
class AlertController(
    private val alertService: AlertService,
) : AlertsApi {

    override suspend fun createAlert(createAlertRequest: CreateAlertRequest): ResponseEntity<AlertResponse> {
        val alert = alertService.createAlert(
            userId = createAlertRequest.userId,
            symbol = createAlertRequest.symbol,
            targetPrice = BigDecimal.valueOf(createAlertRequest.targetPrice),
            condition = createAlertRequest.condition.toDomain(),
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(alert.toResponse())
    }

    override suspend fun deactivateAlert(id: UUID): ResponseEntity<Unit> {
        alertService.deactivateAlert(id)
        return ResponseEntity.noContent().build()
    }
}

// --- Mapping extension functions (Controller layer only) ---

private fun Alert.toResponse(): AlertResponse = AlertResponse(
    id = id,
    userId = userId,
    symbol = symbol,
    targetPrice = targetPrice.toDouble(),
    condition = condition.toResponseCondition(),
    isActive = isActive,
    createdAt = createdAt.atOffset(ZoneOffset.UTC),
)

private fun AlertCondition.toResponseCondition(): AlertResponse.Condition =
    AlertResponse.Condition.valueOf(this.name)

private fun CreateAlertRequest.Condition.toDomain(): AlertCondition =
    AlertCondition.valueOf(this.value)
