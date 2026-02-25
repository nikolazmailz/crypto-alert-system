package com.cryptoalert.marketdata.application

import com.cryptoalert.marketdata.domain.CryptoPrice
import com.cryptoalert.marketdata.domain.CryptoPriceRepository
import org.springframework.stereotype.Service

@Service
class MarketDataService(
    private val repository: CryptoPriceRepository,
) {

    // Метод для сохранения/обновления цен (понадобится нам позже)
    suspend fun savePrice(cryptoPrice: CryptoPrice): CryptoPrice {
        return repository.save(cryptoPrice)
    }

    // В будущем здесь будет логика: если в БД нет цены, сходить во внешнее API (Binance)
    suspend fun getPrice(symbol: String): CryptoPrice? {
        return repository.findById(symbol.uppercase())
    }
}
