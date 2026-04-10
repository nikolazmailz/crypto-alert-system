package com.cryptoalert.marketdata.domain

/**
 * Порт для персистирования и чтения торговых пар.
 * Реализуется в слое Infrastructure.
 */
interface SymbolRepository {

    /**
     * Сохраняет или обновляет список торговых пар (UPSERT).
     * Операция идемпотентна: повторный вызов с теми же данными не создаёт дублей.
     */
    suspend fun upsertAll(symbols: List<Symbol>)

    /**
     * Возвращает все торговые пары с указанным статусом.
     *
     * @param status статус пары, например "TRADING"
     */
    suspend fun findAllByStatus(status: String): List<Symbol>
}
