package com.cryptoalert.marketdata.application

import com.cryptoalert.marketdata.domain.CryptoPrice
import com.cryptoalert.marketdata.domain.CryptoPriceRepository
import com.cryptoalert.marketdata.domain.ExchangeRateProvider
import org.springframework.stereotype.Service
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Service
class MarketDataService(
    private val repository: CryptoPriceRepository,
    private val exchangeRateProvider: ExchangeRateProvider,
) {

    // Метод для сохранения/обновления цен (понадобится нам позже)
    suspend fun savePrice(cryptoPrice: CryptoPrice): CryptoPrice {
        return repository.save(cryptoPrice)
    }

    // В будущем здесь будет логика: если в БД нет цены, сходить во внешнее API (Binance)
    suspend fun getPrice(symbol: String): CryptoPrice? {
        val normalizedSymbol = symbol.uppercase()

        // 1. Пытаемся найти кэшированную цену в нашей БД
        val cachedPrice = repository.findById(normalizedSymbol)
        if (cachedPrice != null) {
            // В будущем тут добавим проверку (например, если цена старше 1 минуты - обновляем)
            return cachedPrice
        }

        // 2. Если в БД пусто, запрашиваем живую цену у внешней биржи (Binance)
        val livePrice = exchangeRateProvider.fetchPrice(normalizedSymbol)
            ?: return null // Возвращаем null, если монета не существует

        // 3. Формируем новую доменную сущность
        val newPrice = CryptoPrice(
            symbol = normalizedSymbol,
            price = livePrice,
            updatedAt = OffsetDateTime.now(ZoneOffset.UTC)
        )

        // 4. Сохраняем в БД и возвращаем
        return repository.save(newPrice)
    }
}
