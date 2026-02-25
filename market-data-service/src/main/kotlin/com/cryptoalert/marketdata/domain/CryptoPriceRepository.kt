package com.cryptoalert.marketdata.domain

interface CryptoPriceRepository {
    suspend fun save(cryptoPrice: CryptoPrice): CryptoPrice
    suspend fun findById(uppercase: String): CryptoPrice?
}
