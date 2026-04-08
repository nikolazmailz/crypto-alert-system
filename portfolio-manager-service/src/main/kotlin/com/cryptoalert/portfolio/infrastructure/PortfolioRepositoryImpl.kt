package com.cryptoalert.portfolio.infrastructure

import com.cryptoalert.portfolio.domain.Portfolio
import com.cryptoalert.portfolio.domain.PortfolioRepository
import com.fasterxml.jackson.databind.ObjectMapper
import io.r2dbc.postgresql.codec.Json
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitSingle
import org.springframework.stereotype.Repository
import java.time.OffsetDateTime
import java.util.UUID

@Repository
class PortfolioRepositoryImpl(
    private val databaseClient: DatabaseClient,
    private val objectMapper: ObjectMapper,
) : PortfolioRepository {

    override suspend fun findByUserId(userId: UUID): Portfolio? =
        databaseClient.sql(
            """
                    select jsonb_build_object(
                    'id', id,
                    'userId', user_id,
                    'assets', assets
                    ) as data
                    from portfolios
                    where user_id = :userId
                """,
        )
            .bind("userId", userId)
            .fetch()
            .one()
            .map {
                val jsonString = (it["data"] as Json).asString()
                objectMapper.readValue(jsonString, Portfolio::class.java)
            }
            .awaitSingleOrNull()


    override suspend fun save(portfolio: Portfolio): Portfolio {
        val assetsJson = objectMapper.writeValueAsString(portfolio.assets)

        // Используем ON CONFLICT для реализации метода save (insert or update)
        return databaseClient.sql(
            """
            INSERT INTO portfolios (id, user_id, assets, created_at, updated_at)
            VALUES (:id, :userId, :assets::jsonb, :now, :now)
            ON CONFLICT (user_id) DO UPDATE SET
                assets = EXCLUDED.assets,
                updated_at = EXCLUDED.updated_at
            RETURNING id, user_id, assets
            """,
        )
            .bind("id", portfolio.id)
            .bind("userId", portfolio.userId)
            .bind("assets", assetsJson)
            .bind("now", OffsetDateTime.now())
            .map { row, _ ->
                // Возвращаем актуальное состояние из БД
                portfolio.copy(
                    id = row.get("id", UUID::class.java)!!,
                    userId = row.get("user_id", UUID::class.java)!!,
                )
            }
            .awaitSingle()
    }
}
