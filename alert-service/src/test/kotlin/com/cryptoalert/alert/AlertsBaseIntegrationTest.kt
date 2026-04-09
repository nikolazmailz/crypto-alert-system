package com.cryptoalert.alert

import com.cryptoalert.BaseIntegrationTest
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureWebTestClient
class AlertsBaseIntegrationTest: BaseIntegrationTest() {

    @Autowired
    lateinit var databaseClient: DatabaseClient

    suspend fun deleteAllAlerts() {
        databaseClient.sql("delete from alerts")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }
}
