package com.cryptoalert.marketdata.infrastructure.persistance

import com.cryptoalert.marketdata.domain.Symbol
import com.cryptoalert.marketdata.domain.SymbolRepository
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.oshai.kotlinlogging.KotlinLogging
import io.r2dbc.postgresql.codec.Json
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository

@Repository
class DatabaseSymbolRepository(
    private val databaseClient: DatabaseClient,
    private val objectMapper: ObjectMapper,
) : SymbolRepository {

    private val log = KotlinLogging.logger {}

    override suspend fun upsertAll(symbols: List<Symbol>) {
        log.debug { "Upserting ${symbols.size} symbols" }
        symbols.forEach { symbol -> upsert(symbol) }
    }

    override suspend fun findAllByStatus(status: String): List<Symbol> {
        return databaseClient.sql(
            """
            select jsonb_build_object(
                'symbol',        s.symbol,
                'baseAsset',     s.base_asset,
                'quoteAsset',    s.quote_asset,
                'status',        s.status,
                'lastUpdatedAt', s.last_updated_at
            ) as data
            from symbols s
            where s.status = :status
            """.trimIndent()
        )
            .bind("status", status)
            .fetch()
            .all()
            .asFlow()
            .map { row ->
                val jsonString = (row["data"] as Json).asString()
                objectMapper.readValue(jsonString, Symbol::class.java)
            }
            .toList()
    }

    private suspend fun upsert(symbol: Symbol) {
        databaseClient.sql(
            """
            insert into symbols (symbol, base_asset, quote_asset, status, last_updated_at)
            values (:symbol, :baseAsset, :quoteAsset, :status, :lastUpdatedAt)
            on conflict (symbol) do update
                set base_asset      = excluded.base_asset,
                    quote_asset     = excluded.quote_asset,
                    status          = excluded.status,
                    last_updated_at = excluded.last_updated_at
            """.trimIndent()
        )
            .bind("symbol", symbol.symbol)
            .bind("baseAsset", symbol.baseAsset)
            .bind("quoteAsset", symbol.quoteAsset)
            .bind("status", symbol.status)
            .bind("lastUpdatedAt", symbol.lastUpdatedAt)
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
