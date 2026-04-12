package com.cryptoalert.account.controller

import com.cryptoalert.account.AccountBaseIntegrationTest
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldNotBeEmpty
import kotlinx.coroutines.runBlocking
import org.springframework.http.MediaType

class AuthControllerIntegrationTest : AccountBaseIntegrationTest() {

    init {
        beforeTest { runBlocking { deleteAllUsers() } }

        context("POST /api/v1/auth/register") {
            should("return 201 and user profile on successful registration") {
                webTestClient.post()
                    .uri("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""{"email":"test@example.com","password":"SecurePass1"}""")
                    .exchange()
                    .expectStatus().isCreated
                    .expectBody()
                    .jsonPath("$.id").isNotEmpty
                    .jsonPath("$.email").isEqualTo("test@example.com")
                    .jsonPath("$.createdAt").isNotEmpty
            }

            should("return 409 when email already exists") {
                val body = """{"email":"dup@example.com","password":"SecurePass1"}"""

                // Первая регистрация
                webTestClient.post()
                    .uri("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .exchange()
                    .expectStatus().isCreated

                // Повторная регистрация с тем же email
                webTestClient.post()
                    .uri("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .exchange()
                    .expectStatus().isEqualTo(409)
            }

            should("return 422 when email is invalid") {
                webTestClient.post()
                    .uri("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""{"email":"not-an-email@asd","password":"SecurePass1"}""")
                    .exchange()
                    .expectStatus().isEqualTo(422)
            }

            should("return 422 when password is too short") {
                webTestClient.post()
                    .uri("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""{"email":"valid@example.com","password":"123"}""")
                    .exchange()
                    .expectStatus().isEqualTo(422)
            }
        }

        context("POST /api/v1/auth/login") {
            should("return 200 with JWT token on valid credentials") {
                // Arrange: зарегистрируем пользователя
                webTestClient.post()
                    .uri("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""{"email":"login@example.com","password":"SecurePass1"}""")
                    .exchange()
                    .expectStatus().isCreated

                // Act: логин
                val response = webTestClient.post()
                    .uri("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""{"email":"login@example.com","password":"SecurePass1"}""")
                    .exchange()
                    .expectStatus().isOk
                    .expectBody()
                    .jsonPath("$.token").isNotEmpty
                    .jsonPath("$.expiresIn").isNotEmpty
                    .returnResult()

                response shouldNotBe null
            }

            should("return 401 on invalid password") {
                webTestClient.post()
                    .uri("/api/v1/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""{"email":"user2@example.com","password":"CorrectPass1"}""")
                    .exchange()
                    .expectStatus().isCreated

                webTestClient.post()
                    .uri("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""{"email":"user2@example.com","password":"WrongPass1"}""")
                    .exchange()
                    .expectStatus().isUnauthorized
            }

            should("return 401 on non-existent email") {
                webTestClient.post()
                    .uri("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""{"email":"ghost@example.com","password":"SomePass1"}""")
                    .exchange()
                    .expectStatus().isUnauthorized
            }
        }
    }
}
