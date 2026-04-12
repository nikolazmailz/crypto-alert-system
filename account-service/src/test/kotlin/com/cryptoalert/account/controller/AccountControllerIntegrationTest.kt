package com.cryptoalert.account.controller

import com.cryptoalert.account.AccountBaseIntegrationTest
import kotlinx.coroutines.runBlocking
import org.springframework.http.MediaType
import java.util.UUID

class AccountControllerIntegrationTest : AccountBaseIntegrationTest() {

    init {
        beforeTest { runBlocking { deleteAllUsers() } }

        context("GET /api/v1/accounts/me") {
            should("return 200 with profile when authenticated") {
                // 1. Регистрируем пользователя
                val registerBody = webTestClient.post()
                    .uri("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""{"email":"me@example.com","password":"SecurePass1"}""")
                    .exchange()
                    .expectStatus().isCreated
                    .expectBody(String::class.java)
                    .returnResult()
                    .responseBody ?: error("No register response")

                // 2. Извлекаем id зарегистрированного пользователя
                val userId = UUID.fromString(
                    objectMapper.readTree(registerBody).get("id").asText(),
                )

                // 3. Запрашиваем профиль с токеном, сгенерированным из этого userId
                webTestClient.get()
                    .uri("/api/v1/accounts/me")
                    .header("Authorization", bearerToken(userId))
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(userId.toString())
                    .jsonPath("$.email").isEqualTo("me@example.com")
                    .jsonPath("$.createdAt").isNotEmpty
            }

            should("return 401 when no Authorization header") {
                webTestClient.get()
                    .uri("/api/v1/accounts/me")
                    .exchange()
                    .expectStatus().isUnauthorized
            }

            should("return 401 when Bearer token is malformed") {
                webTestClient.get()
                    .uri("/api/v1/accounts/me")
                    .header("Authorization", "Bearer not.a.valid.jwt")
                    .exchange()
                    .expectStatus().isUnauthorized
            }

            should("return 401 when Authorization header has wrong scheme") {
                webTestClient.get()
                    .uri("/api/v1/accounts/me")
                    .header("Authorization", "Basic dXNlcjpwYXNz")
                    .exchange()
                    .expectStatus().isUnauthorized
            }
        }
    }
}
