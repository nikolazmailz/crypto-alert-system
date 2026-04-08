package com.cryptoalert.portfolio

import com.cryptoalert.BaseIntegrationTest
import com.cryptoalert.marketdata.api.PriceProvider
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.ActiveProfiles
import com.ninjasquad.springmockk.MockkBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
abstract class PortfolioBaseIntegrationTest: BaseIntegrationTest() {

    @Autowired
    lateinit var databaseClient: DatabaseClient
    @Autowired
    lateinit var webTestClient: WebTestClient

    @MockkBean
    lateinit var priceProvider: PriceProvider

    suspend fun deleteAllPortfolios() {
        databaseClient.sql("DELETE FROM portfolios")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
