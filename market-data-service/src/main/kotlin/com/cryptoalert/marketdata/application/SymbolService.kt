package com.cryptoalert.marketdata.application

import com.cryptoalert.marketdata.domain.Symbol
import com.cryptoalert.marketdata.domain.SymbolRepository
import org.springframework.stereotype.Service

/**
 * Use-case: предоставление списка активных торговых пар.
 * Тонкий сервис — делегирует в репозиторий без бизнес-логики поверх.
 */
@Service
class SymbolService(
    private val symbolRepository: SymbolRepository,
) {

    suspend fun getActiveSymbols(): List<Symbol> =
        symbolRepository.findAllByStatus(TRADING_STATUS)

    private companion object {
        const val TRADING_STATUS = "TRADING"
    }
}
