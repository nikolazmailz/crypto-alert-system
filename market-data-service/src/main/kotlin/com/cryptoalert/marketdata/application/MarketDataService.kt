package com.cryptoalert.marketdata.application

import com.cryptoalert.marketdata.api.PriceProvider
import com.cryptoalert.marketdata.domain.CryptoPrice
import com.cryptoalert.marketdata.domain.CryptoPriceRepository
import com.cryptoalert.marketdata.domain.ExchangeRateProvider
import com.cryptoalert.shared.error.ResourceNotFoundException
import com.cryptoalert.shared.event.PriceChangedEvent
import com.cryptoalert.shared.event.PriceEventPublisher
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneOffset

@Service
class MarketDataService(
    private val repository: CryptoPriceRepository,
    private val exchangeRateProvider: ExchangeRateProvider,
    private val eventPublisher: PriceEventPublisher,
): PriceProvider {

    // Метод для сохранения/обновления цен (понадобится нам позже)
    suspend fun savePrice(cryptoPrice: CryptoPrice): CryptoPrice {
        return repository.save(cryptoPrice)
    }

    suspend fun getPrice(symbol: String): CryptoPrice {
        val normalizedSymbol = symbol.uppercase()

        // Пытаемся найти в БД, если нет — идем во внешний провайдер через элвис-оператор
        return repository.findById(normalizedSymbol)
            ?: exchangeRateProvider.fetchPrice(normalizedSymbol)?.let { livePrice ->
                // Если получили цену извне — создаем объект, сохраняем и возвращаем его
                val newPrice = CryptoPrice(
                    symbol = normalizedSymbol,
                    price = livePrice,
                    updatedAt = OffsetDateTime.now(ZoneOffset.UTC)
                )
                eventPublisher.publish(PriceChangedEvent(symbol, newPrice.price))
                repository.save(newPrice)
            } ?: throw ResourceNotFoundException("Crypto pair $symbol not found")
    }

    override suspend fun getCurrentPrice(symbol: String): BigDecimal {
        // Вызываем уже написанную нами логику (с кэшем и Binance)
        return getPrice(symbol).price
    }
}
