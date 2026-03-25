package com.cryptoalert.marketdata

import com.cryptoalert.BaseIntegrationTest
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class MarketDataBaseIntegrationTest(
    private val databaseClient: DatabaseClient,
): BaseIntegrationTest() {

    suspend fun deleteAllCryptoPrices() {
        databaseClient.sql("delete from crypto_prices")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

}
