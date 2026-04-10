package com.cryptoalert.marketdata.controller

import com.cryptoalert.marketdata.api.SymbolsApi
import com.cryptoalert.marketdata.application.SymbolService
import com.cryptoalert.marketdata.domain.Symbol
import com.cryptoalert.marketdata.dto.SymbolResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Тонкий REST-контроллер для эндпоинта GET /api/v1/market-data/symbols.
 * Реализует сгенерированный интерфейс [SymbolsApi] (OpenAPI Generator).
 *
 * Генератор с `reactive=true, useCoroutines=true` производит для массивов
 * `fun ... : ResponseEntity<Flow<T>>` — Flow уже является coroutine-нативным
 * асинхронным стримом и не требует `suspend`. Вызов suspend-метода сервиса
 * размещаем внутри `flow { }` builder'а, который сам является coroutine-контекстом.
 */
@RestController
class SymbolsController(
    private val symbolService: SymbolService,
) : SymbolsApi {

    override fun getSymbols(): ResponseEntity<Flow<SymbolResponse>> {
        val responseFlow = flow {
            symbolService.getActiveSymbols().forEach { emit(it.toResponse()) }
        }
        return ResponseEntity.ok(responseFlow)
    }
}

// Extension-функция маппинга Domain → DTO, вынесена за пределы класса (CLAUDE.md)
private fun Symbol.toResponse(): SymbolResponse = SymbolResponse(
    symbol = symbol,
    baseAsset = baseAsset,
    quoteAsset = quoteAsset,
    status = status,
)
