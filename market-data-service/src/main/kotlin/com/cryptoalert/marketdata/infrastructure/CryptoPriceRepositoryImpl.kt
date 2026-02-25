package com.cryptoalert.marketdata.infrastructure

import com.cryptoalert.marketdata.domain.CryptoPrice
import com.cryptoalert.marketdata.domain.CryptoPriceRepository
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import io.r2dbc.postgresql.codec.Json
import kotlinx.coroutines.reactor.awaitSingleOrNull

@Repository
class CryptoPriceRepositoryImpl(
    private val databaseClient: DatabaseClient,
    private val objectMapper: ObjectMapper
): CryptoPriceRepository {

    override suspend fun save(cryptoPrice: CryptoPrice): CryptoPrice {
        databaseClient.sql(
            """
            insert into crypto_prices (symbol, price, updated_at)
            values (:symbol, :price, :updatedAt)
            on conflict (symbol) do update
            set price = excluded.price, updated_at = excluded.updated_at
            """.trimIndent()
        )
            .bind("symbol", cryptoPrice.symbol)
            .bind("price", cryptoPrice.price)
            .bind("updatedAt", cryptoPrice.updatedAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle() // Ждем завершения операции

        return cryptoPrice
    }

    override suspend fun findById(symbol: String): CryptoPrice? {
        return databaseClient.sql(
            """
            select jsonb_build_object(
                'symbol', cp.symbol,
                'price', cp.price,
                'updatedAt', cp.updated_at
            ) as data
            from crypto_prices cp
            where cp.symbol = :symbol
            """.trimIndent()
        )
            .bind("symbol", symbol.uppercase())
            .fetch()
            .one()
            .map { row ->
                // R2DBC драйвер PostgreSQL возвращает JSON как io.r2dbc.postgresql.codec.Json
                val jsonString = (row["data"] as Json).asString()
                objectMapper.readValue(jsonString, CryptoPrice::class.java)
            }
            .awaitSingleOrNull()
    }
}
