package com.cryptoalert.marketdata.domain

/**
 * Порт для получения информации о доступных торговых парах у внешнего провайдера.
 * Домен декларирует потребность — Infrastructure предоставляет реализацию (Binance).
 */
interface SymbolInfoProvider {

    /**
     * Возвращает список всех доступных торговых пар из внешнего источника.
     * При ошибке сети или провайдера возвращает пустой список.
     */
    suspend fun fetchExchangeInfo(): List<Symbol>
}
