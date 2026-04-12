package com.cryptoalert.account

import com.cryptoalert.BaseIntegrationTest
import com.cryptoalert.shared.security.JwtTokenService
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureWebTestClient
abstract class AccountBaseIntegrationTest : BaseIntegrationTest() {

    @Autowired
    lateinit var tokenService: JwtTokenService

    @Autowired
    lateinit var webTestClient: WebTestClient

    @Autowired
    lateinit var databaseClient: DatabaseClient

    @Autowired
    lateinit var objectMapper: ObjectMapper

    suspend fun deleteAllUsers() {
        databaseClient
            .sql("DELETE FROM users")
            .fetch()
            .rowsUpdated()
            .awaitSingle()
    }

    /** Генерирует валидный Bearer-токен для указанного userId (для тестов). */
    fun bearerToken(userId: UUID): String = "Bearer ${tokenService.generateToken(userId)}"
}
